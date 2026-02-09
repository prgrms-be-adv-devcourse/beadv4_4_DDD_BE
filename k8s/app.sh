#!/bin/bash
#
# Modeunsa 애플리케이션 배포 스크립트 (k3s)
#
# 사전 조건:
#   - infra가 먼저 실행되어 있어야 함 (./k8s/infra.sh up)
#   - Docker 이미지가 빌드되어 있어야 함
#
# 사용법:
#   ./k8s/app.sh up [dev|prod]  앱 배포
#                               - dev: NodePort (기본값)
#                               - prod: ClusterIP + Ingress
#   ./k8s/app.sh down           앱 중지
#   ./k8s/app.sh status         앱 상태 확인
#   ./k8s/app.sh restart        앱 재시작
#   ./k8s/app.sh logs           앱 로그 확인
#
# 접속 정보 (dev - NodePort):
#   API Server    localhost:30080
#   Frontend      localhost:30000
#
# 접속 정보 (prod - Ingress):
#   API Server    <EC2-IP>/api
#   Frontend      <EC2-IP>/
#

NAMESPACE="modeunsa"
RELEASE="modeunsa-app"
CHART_DIR="$(dirname "$0")/app"
ROOT_DIR="$(dirname "$0")/.."

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

case "$1" in
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

    # .env 로드
    if [ -f "$ENV_FILE" ]; then
      echo "환경 파일 로드: $ENV_FILE"
      source "$ENV_FILE"
    else
      echo ".env 파일을 찾을 수 없습니다: $ENV_FILE"
      echo "다음 명령어로 생성하세요: cp .env.example $ENV_FILE"
      exit 1
    fi

    # Docker 이미지 확인 (API)
    if [ -z "$DOCKER_IMAGE" ]; then
      echo "DOCKER_IMAGE 환경변수가 설정되지 않았습니다."
      exit 1
    fi

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

    # Docker 이미지 repository와 tag 분리
    IMAGE_REPO="${DOCKER_IMAGE%:*}"
    IMAGE_TAG="${DOCKER_IMAGE##*:}"

    if [ "$FRONTEND_ENABLED" = "true" ]; then
      FRONTEND_REPO="${FRONTEND_IMAGE%:*}"
      FRONTEND_TAG="${FRONTEND_IMAGE##*:}"
    fi

    # ARM Mac에서 x86 이미지 사용 시 k3s에 직접 import (dev 환경에서만)
    if [ "$ENV" = "dev" ] && colima status &>/dev/null; then
      echo "Docker 이미지를 k3s에 import 합니다..."

      # API 이미지
      echo "  API: $DOCKER_IMAGE"
      docker pull --platform linux/amd64 "$DOCKER_IMAGE" 2>&1 | tail -1
      docker save "$DOCKER_IMAGE" | \
        colima ssh -- sudo ctr -a /run/containerd/containerd.sock -n k8s.io images import --no-unpack -

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
      --set api.image.repository="$IMAGE_REPO"
      --set api.image.tag="$IMAGE_TAG"
      --set api.env.dbName="$MYSQL_DATABASE"
      --set api.secrets.dbPassword="$MYSQL_ROOT_PASSWORD"
      --set api.secrets.redisPassword="$REDIS_PASSWORD"
      --set api.secrets.jwtSecret="$JWT_SECRET"
      --set api.secrets.kakaoClientId="$KAKAO_CLIENT_ID"
      --set api.secrets.kakaoClientSecret="$KAKAO_CLIENT_SECRET"
      --set api.secrets.naverClientId="$NAVER_CLIENT_ID"
      --set api.secrets.naverClientSecret="$NAVER_CLIENT_SECRET"
      --set api.secrets.awsAccessKey="$AWS_ACCESS_KEY"
      --set api.secrets.awsSecretKey="$AWS_SECRET_KEY"
      --set api.secrets.encryptionMasterKey="$ENCRYPTION_MASTER_KEY"
      --set api.secrets.tossPaymentsSecretKey="$TOSS_PAYMENTS_SECRET_KEY"
      --set api.secrets.internalApiKey="$INTERNAL_API_KEY"
      --set frontend.enabled="$FRONTEND_ENABLED"
    )

    # Frontend 설정 추가 (enabled인 경우만)
    if [ "$FRONTEND_ENABLED" = "true" ]; then
      HELM_ARGS+=(
        --set frontend.image.repository="$FRONTEND_REPO"
        --set frontend.image.tag="$FRONTEND_TAG"
      )
    fi

    helm upgrade --install $RELEASE $CHART_DIR -n $NAMESPACE $VALUES_FILES "${HELM_ARGS[@]}"

    # Pod가 Ready 될 때까지 대기
    echo "Waiting for pods to be ready..."
    kubectl wait --for=condition=ready pod -l app=$RELEASE-api -n $NAMESPACE --timeout=120s

    if [ "$FRONTEND_ENABLED" = "true" ]; then
      kubectl wait --for=condition=ready pod -l app=$RELEASE-frontend -n $NAMESPACE --timeout=120s
    fi

    echo ""
    echo "=== Application deployed ==="
    if [ "$ENV" = "dev" ]; then
      echo "  API Server → localhost:30080"
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
    kubectl rollout restart deployment/$RELEASE-api -n $NAMESPACE
    kubectl rollout restart deployment/$RELEASE-frontend -n $NAMESPACE 2>/dev/null
    echo "=== Application restarted ==="
    ;;

  logs)
    case "$2" in
      frontend) kubectl logs -f -l app=$RELEASE-frontend -n $NAMESPACE ;;
      api|"")   kubectl logs -f -l app=$RELEASE-api -n $NAMESPACE ;;
      *)        echo "Usage: $0 logs [api|frontend]" ;;
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
