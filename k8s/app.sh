#!/bin/bash
#
# Modeunsa 애플리케이션 배포 스크립트 (k3s)
#
# 사전 조건:
#   - infra가 먼저 실행되어 있어야 함 (./k8s/infra.sh up)
#   - Docker 이미지가 빌드되어 있어야 함
#
# 사용법:
#   ./k8s/app.sh up [dev|prod]    앱 배포 (기본: dev)
#   ./k8s/app.sh down             앱 중지
#   ./k8s/app.sh status           앱 상태 확인
#   ./k8s/app.sh restart          앱 재시작
#   ./k8s/app.sh logs             앱 로그 확인
#
# 접속 정보 (dev - NodePort):
#   API Server    localhost:30080
#   Product       localhost:30081
#   Order         localhost:30082
#   Payment       localhost:30083
#   Settlement    localhost:30084
#   File          localhost:30088
#   Inventory     localhost:30089
#   Frontend      localhost:30000
#   Member        localhost:30086

#
# 접속 정보 (prod - Ingress):
#   API Server    <EC2-IP>/api
#   Frontend      <EC2-IP>/
#

NAMESPACE="modeunsa"
RELEASE="modeunsa-app"
CHART_DIR="$(dirname "$0")/app"
ROOT_DIR="$(dirname "$0")/.."
POD_READY_TIMEOUT="${POD_READY_TIMEOUT:-60s}"
WAIT_IN_BACKGROUND="${WAIT_IN_BACKGROUND:-true}"
WAIT_LOG_FILE="${WAIT_LOG_FILE:-/tmp/modeunsa-app-pod-wait.log}"

# 백엔드 모듈 메타데이터
# 형식: module|IMAGE_ENV|표시명|dev접속주소|required(optional/required)
BACKEND_MODULES=(
  "api-gateway-api|API_GATEWAY_IMAGE|API Gateway|localhost:30080|required"
  "product-api|PRODUCT_IMAGE|Product|localhost:30081|optional"
  "order-api|ORDER_IMAGE|Order|localhost:30082|optional"
  "payment-api|PAYMENT_IMAGE|Payment|localhost:30083|optional"
  "settlement-api|SETTLEMENT_IMAGE|Settlement|localhost:30084|optional"
  "member-api|MEMBER_IMAGE|Member|localhost:30086|optional"
  "file-api|FILE_IMAGE|File|localhost:30088|optional"
  "inventory-api|INVENTORY_IMAGE|Inventory|localhost:30089|optional"
)

# k3s 환경에서 KUBECONFIG 자동 설정
if [ -z "$KUBECONFIG" ] && [ -f /etc/rancher/k3s/k3s.yaml ]; then
  export KUBECONFIG=/etc/rancher/k3s/k3s.yaml
fi

# 환경 인자 처리 (dev/prod, 기본값: dev)
get_env_file() {
  local env="${1:-dev}"
  case "$env" in
    dev|prod)
      echo "$ROOT_DIR/.env.k3s-$env"
      ;;
    *)
      echo "오류: 환경은 'dev' 또는 'prod'만 가능합니다." >&2
      exit 1
      ;;
  esac
}

# 백엔드 모듈 공통 helm --set 인자 추가
add_backend_helm_args() {
  local prefix="$1"
  local image_repo="$2"
  local image_tag="$3"

  HELM_ARGS+=(
    --set "$prefix.image.repository=$image_repo"
    --set "$prefix.image.tag=$image_tag"
    --set "$prefix.env.dbName=$MYSQL_DATABASE"
    --set "$prefix.secrets.dbPassword=$MYSQL_ROOT_PASSWORD"
    --set "$prefix.secrets.redisPassword=$REDIS_PASSWORD"
    --set "$prefix.secrets.jwtSecret=$JWT_SECRET"
    --set "$prefix.secrets.kakaoClientId=$KAKAO_CLIENT_ID"
    --set "$prefix.secrets.kakaoClientSecret=$KAKAO_CLIENT_SECRET"
    --set "$prefix.secrets.naverClientId=$NAVER_CLIENT_ID"
    --set "$prefix.secrets.naverClientSecret=$NAVER_CLIENT_SECRET"
    --set "$prefix.secrets.awsAccessKey=$AWS_ACCESS_KEY"
    --set "$prefix.secrets.awsSecretKey=$AWS_SECRET_KEY"
    --set "$prefix.secrets.encryptionMasterKey=$ENCRYPTION_MASTER_KEY"
    --set "$prefix.secrets.tossPaymentsSecretKey=$TOSS_PAYMENTS_SECRET_KEY"
    --set "$prefix.secrets.internalApiKey=$INTERNAL_API_KEY"
    --set "$prefix.secrets.openaiApiKey=$OPENAI_API_KEY"
    --set "$prefix.env.eventPublisherType=$EVENT_PUBLISHER_TYPE"
    --set "$prefix.env.eventConsumerType=$EVENT_CONSUMER_TYPE"
  )
}

get_module_image() {
  local image_env="$1"
  printf '%s' "${!image_env}"
}

case "$1" in
  rollout)
      ENV="${2:-prod}"
      TARGET_MODULE="$3" # deploy.yml에서 넘겨준 모듈명 (예: member-api)

      if [ -z "$TARGET_MODULE" ]; then
        echo "업데이트할 대상 모듈을 지정해야 합니다. (예: $0 rollout prod member-api)"
        exit 1
      fi

      # 서버에 파드가 아예 없는지(초기 상태인지) 검사
      if ! kubectl get deployment "$RELEASE-$TARGET_MODULE" -n "$NAMESPACE" &> /dev/null; then
        echo "[$TARGET_MODULE] 배포된 파드(Deployment)가 서버에 존재하지 않습니다!"
        echo "롤링 업데이트 대신, 초기 배포(up) 로직으로 자동 전환합니다."

        # 현재 쉘의 실행 흐름을 'up' 명령어로 완전히 넘깁니다.
        exec "$0" up "$ENV"
      fi
      # ====================================================================

      ENV_FILE=$(get_env_file "$ENV")
      if [ -f "$ENV_FILE" ]; then
        source "$ENV_FILE"
      else
        echo ".env 파일을 찾을 수 없습니다: $ENV_FILE"
        exit 1
      fi

      echo "=== [$TARGET_MODULE] 모듈의 롤링 업데이트를 시작합니다 ($ENV 환경) ==="

      # 1. 새 이미지 주소 추출
      if [ "$TARGET_MODULE" == "frontend" ]; then
         NEW_IMAGE="$FRONTEND_IMAGE"
      else
         # 'member-api' -> 'MEMBER_IMAGE' 환경변수명으로 변환
         PREFIX=$(echo "$TARGET_MODULE" | sed 's/-api//' | tr 'a-z-' 'A-Z_')
         ENV_VAR_NAME="${PREFIX}_IMAGE"
         NEW_IMAGE="${!ENV_VAR_NAME}"
      fi

      if [ -z "$NEW_IMAGE" ]; then
         echo "에러: $TARGET_MODULE 에 대한 이미지 주소를 .env 파일에서 찾을 수 없습니다."
         exit 1
      fi

      echo "새 이미지: $NEW_IMAGE"

      # 2. 쿠버네티스 Deployment 이미지 교체 (이 명령어 하나로 무중단 롤링 업데이트가 발생합니다)
      kubectl set image deployment/$RELEASE-$TARGET_MODULE \
        $TARGET_MODULE=$NEW_IMAGE -n $NAMESPACE

      # 3. 롤링 업데이트 완료 대기 (이전 파드가 완전히 죽고 새 파드가 뜰 때까지 대기)
      echo "[$TARGET_MODULE] 새 파드가 준비될 때까지 대기합니다..."
      kubectl rollout status deployment/$RELEASE-$TARGET_MODULE -n $NAMESPACE --timeout=$POD_READY_TIMEOUT

      echo "=== [$TARGET_MODULE] 배포 완료! ==="
      ;;
  up)
    ENV="${2:-dev}"  # 기본값 dev
    ENV_FILE=$(get_env_file "$ENV")

    if [ "$ENV" = "dev" ]; then
      VALUES_FILES=""  # values.yaml만 사용 (기본값)
      echo "=== dev 환경으로 배포합니다 (NodePort) ==="
    elif [ "$ENV" = "prod" ]; then
      VALUES_FILES="-f $CHART_DIR/values-prod.yaml"  # prod 설정으로 덮어쓰기
      echo "=== prod 환경으로 배포합니다 (ClusterIP + Ingress) ==="
    else
      echo "알 수 없는 환경: $ENV"
      echo "사용법: $0 up [dev|prod]"
      exit 1
    fi

    ENV_FILE=$(get_env_file "$2")
    # .env 로드
    if [ -f "$ENV_FILE" ]; then
      echo "환경 파일 로드: $ENV_FILE"
      source "$ENV_FILE"
    else
      echo ".env 파일을 찾을 수 없습니다: $ENV_FILE"
      echo "다음 명령어로 생성하세요: cp .env.example $ENV_FILE"
      exit 1
    fi

    ACTIVE_BACKEND_MODULES=()
    HELM_BACKEND_ARGS=()

    # 백엔드 모듈 이미지 확인 및 enabled 설정
    for module_info in "${BACKEND_MODULES[@]}"; do
      IFS='|' read -r module image_env display_name dev_addr requirement <<<"$module_info"
      image_value=$(get_module_image "$image_env")

      if [ -n "$image_value" ]; then
        ACTIVE_BACKEND_MODULES+=("$module|$image_value|$display_name|$dev_addr")
        HELM_BACKEND_ARGS+=(--set "$module.enabled=true")
      else
        HELM_BACKEND_ARGS+=(--set "$module.enabled=false")
        if [ "$requirement" = "required" ]; then
          echo "$image_env 환경변수가 설정되지 않았습니다."
          exit 1
        fi
        echo "$image_env 환경변수가 없습니다. $display_name 모듈은 배포하지 않습니다."
      fi
    done

    # Docker 이미지 확인 (Frontend) - 선택적
    if [ -z "$FRONTEND_IMAGE" ]; then
      echo "FRONTEND_IMAGE 환경변수가 없습니다. Frontend는 배포하지 않습니다."
      FRONTEND_ENABLED="false"
    else
      FRONTEND_ENABLED="true"
    fi

    # namespace 확인
    if ! kubectl get namespace $NAMESPACE &>/dev/null; then
      echo "namespace '$NAMESPACE'가 없습니다. 먼저 infra를 시작하세요: ./k8s/infra.sh up"
      exit 1
    fi

    if [ "$FRONTEND_ENABLED" = "true" ]; then
      FRONTEND_REPO="${FRONTEND_IMAGE%:*}"
      FRONTEND_TAG="${FRONTEND_IMAGE##*:}"
    fi

    # ARM Mac에서 x86 이미지 사용 시 k3s에 직접 import (dev 환경에서만)
    if [ "$ENV" = "dev" ] && colima status &>/dev/null; then
      echo "Docker 이미지를 k3s에 import 합니다..."

      # Backend 이미지
      for backend_info in "${ACTIVE_BACKEND_MODULES[@]}"; do
        IFS='|' read -r module image_value display_name dev_addr <<<"$backend_info"
        echo "  $display_name: $image_value"
        docker pull --platform linux/amd64 "$image_value" 2>&1 | tail -1
        docker save "$image_value" | \
          colima ssh -- sudo ctr -a /run/containerd/containerd.sock -n k8s.io images import --no-unpack -
      done

      # Frontend 이미지
      if [ "$FRONTEND_ENABLED" = "true" ]; then
        echo "  Frontend: $FRONTEND_IMAGE"
        docker pull --platform linux/amd64 "$FRONTEND_IMAGE" 2>&1 | tail -1
        docker save "$FRONTEND_IMAGE" | \
          colima ssh -- sudo ctr -a /run/containerd/containerd.sock -n k8s.io images import --no-unpack -
      fi

      echo "이미지 import 완료"
    fi

    # helm 설치 또는 업그레이드
    HELM_ARGS=(
      "${HELM_BACKEND_ARGS[@]}"
      --set frontend.enabled="$FRONTEND_ENABLED"
    )

    # 활성화된 백엔드 모듈 공통 설정
    for backend_info in "${ACTIVE_BACKEND_MODULES[@]}"; do
      IFS='|' read -r module image_value display_name dev_addr <<<"$backend_info"
      image_repo="${image_value%:*}"
      image_tag="${image_value##*:}"
      add_backend_helm_args "$module" "$image_repo" "$image_tag"
    done

    # Frontend 설정 추가 (enabled인 경우만)
    if [ "$FRONTEND_ENABLED" = "true" ]; then
      HELM_ARGS+=(
        --set frontend.image.repository="$FRONTEND_REPO"
        --set frontend.image.tag="$FRONTEND_TAG"
        --set frontend.secrets.jwtSecret="$JWT_SECRET"
      )
    fi

    helm upgrade --install $RELEASE $CHART_DIR -n $NAMESPACE $VALUES_FILES "${HELM_ARGS[@]}"

    # Pod Ready 대기 (기본: 백그라운드)
    echo "Waiting for pods to be ready... (timeout: $POD_READY_TIMEOUT)"
    if [ "$WAIT_IN_BACKGROUND" = "true" ]; then
      (
        echo "[INFO] Pod readiness check started at $(date)"
        failed=0

        for backend_info in "${ACTIVE_BACKEND_MODULES[@]}"; do
          IFS='|' read -r module image_value display_name dev_addr <<<"$backend_info"
          echo "[INFO] waiting: $display_name (app=$RELEASE-$module)"
          if ! kubectl wait --for=condition=ready pod -l app=$RELEASE-$module -n $NAMESPACE --timeout="$POD_READY_TIMEOUT"; then
            echo "[ERROR] timeout: $display_name (app=$RELEASE-$module)"
            kubectl get pods -n "$NAMESPACE" -l app="$RELEASE-$module" -o wide || true
            failed=1
          fi
        done

        if [ "$FRONTEND_ENABLED" = "true" ]; then
          echo "[INFO] waiting: Frontend (app=$RELEASE-frontend)"
          if ! kubectl wait --for=condition=ready pod -l app=$RELEASE-frontend -n $NAMESPACE --timeout="$POD_READY_TIMEOUT"; then
            echo "[ERROR] timeout: Frontend (app=$RELEASE-frontend)"
            kubectl get pods -n "$NAMESPACE" -l app="$RELEASE-frontend" -o wide || true
            failed=1
          fi
        fi

        if [ "$failed" -eq 0 ]; then
          echo "[SUCCESS] all target pods are ready"
        else
          echo "[FAILED] some pods did not become ready in time"
          exit 1
        fi
      ) >"$WAIT_LOG_FILE" 2>&1 &

      WAIT_PID=$!
      echo "Pod readiness check running in background."
      echo "  PID: $WAIT_PID"
      echo "  LOG: $WAIT_LOG_FILE"
      echo "  tail -f $WAIT_LOG_FILE"
    else
      for backend_info in "${ACTIVE_BACKEND_MODULES[@]}"; do
        IFS='|' read -r module image_value display_name dev_addr <<<"$backend_info"
        if ! kubectl wait --for=condition=ready pod -l app=$RELEASE-$module -n $NAMESPACE --timeout="$POD_READY_TIMEOUT"; then
          echo "[ERROR] Timeout waiting for $display_name pods"
          kubectl get pods -n "$NAMESPACE" -l app="$RELEASE-$module" -o wide || true
          exit 1
        fi
      done

      if [ "$FRONTEND_ENABLED" = "true" ]; then
        if ! kubectl wait --for=condition=ready pod -l app=$RELEASE-frontend -n $NAMESPACE --timeout="$POD_READY_TIMEOUT"; then
          echo "[ERROR] Timeout waiting for Frontend pods"
          kubectl get pods -n "$NAMESPACE" -l app="$RELEASE-frontend" -o wide || true
          exit 1
        fi
      fi
    fi

    echo ""
    echo "=== Application deployed ==="
    if [ "$ENV" = "dev" ]; then
      for backend_info in "${ACTIVE_BACKEND_MODULES[@]}"; do
        IFS='|' read -r module image_value display_name dev_addr <<<"$backend_info"
        echo "  $display_name → $dev_addr"
      done
      if [ "$FRONTEND_ENABLED" = "true" ]; then
        echo "  Frontend   → localhost:30000"
      fi
    else
      echo "  서비스들이 ClusterIP로 실행됩니다."
      echo "  API Server → <EC2-IP>/api (Ingress)"
      if [ "$FRONTEND_ENABLED" = "true" ]; then
        echo "  Frontend   → <EC2-IP>/ (Ingress)"
      fi
    fi
    ;;

  down)
    helm uninstall $RELEASE -n $NAMESPACE 2>/dev/null
    echo "=== Application stopped ==="
    ;;

  status)
    LABEL="app.kubernetes.io/instance=$RELEASE"
    case "$2" in
      pod|pods|"")  kubectl get pods -n $NAMESPACE -l "$LABEL" ;;
      deploy*)      kubectl get deployments -n $NAMESPACE -l "$LABEL" ;;
      svc|service*) kubectl get svc -n $NAMESPACE -l "$LABEL" ;;
      all)          kubectl get all -n $NAMESPACE -l "$LABEL" ;;
      *)            echo "Usage: $0 status [pod|deploy|svc|all]" ;;
    esac
    ;;

  restart)
    for module_info in "${BACKEND_MODULES[@]}"; do
      IFS='|' read -r module image_env display_name dev_addr requirement <<<"$module_info"
      kubectl rollout restart deployment/$RELEASE-$module -n $NAMESPACE 2>/dev/null
    done
    kubectl rollout restart deployment/$RELEASE-frontend -n $NAMESPACE 2>/dev/null
    echo "=== Application restarted ==="
    ;;

  logs)
    case "$2" in
      frontend) kubectl logs -f -l app=$RELEASE-frontend -n $NAMESPACE ;;
      api|"")   kubectl logs -f -l app=$RELEASE-api -n $NAMESPACE ;;
      settlement|settlement-api) kubectl logs -f -l app=$RELEASE-settlement-api -n $NAMESPACE ;;
      *)
        for module_info in "${BACKEND_MODULES[@]}"; do
          IFS='|' read -r module image_env display_name dev_addr requirement <<<"$module_info"
          if [ "$2" = "$module" ]; then
            kubectl logs -f -l app=$RELEASE-$module -n $NAMESPACE
            exit 0
          fi
        done
        echo "Usage: $0 logs [api|settlement-api|frontend]"
        ;;
    esac
    ;;

  *)
    echo "Usage: $0 {up|down|status|restart|logs} [dev|prod]"
    echo ""
    echo "Examples:"
    echo "  $0 up dev       # dev 환경으로 배포"
    echo "  $0 up prod      # prod 환경으로 배포"
    echo "  $0 down         # 중지"
    echo "  $0 logs         # 로그 확인"
    ;;
esac
