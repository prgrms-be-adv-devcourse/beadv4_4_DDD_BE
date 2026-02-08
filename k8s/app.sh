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
# 접속 정보:
#   API Server    localhost:30080
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

    # Docker 이미지 확인
    if [ -z "$DOCKER_IMAGE" ]; then
      echo "DOCKER_IMAGE 환경변수가 설정되지 않았습니다."
      echo ".env 파일에 DOCKER_IMAGE=your-image:tag 형식으로 설정해주세요."
      exit 1
    fi

    # namespace 확인 (infra가 먼저 실행되어 있어야 함)
    if ! kubectl get namespace $NAMESPACE &>/dev/null; then
      echo "namespace '$NAMESPACE'가 없습니다. 먼저 infra를 시작하세요: ./k8s/infra.sh up"
      exit 1
    fi

    # Docker 이미지 repository와 tag 분리
    IMAGE_REPO="${DOCKER_IMAGE%:*}"
    IMAGE_TAG="${DOCKER_IMAGE##*:}"

    # ARM Mac에서 x86 이미지 사용 시 k3s에 직접 import
    if colima status &>/dev/null; then
      echo "Docker 이미지를 k3s에 import 합니다... ($DOCKER_IMAGE)"
      docker pull --platform linux/amd64 "$DOCKER_IMAGE" 2>&1 | tail -1
      docker save "$DOCKER_IMAGE" | \
        colima ssh -- sudo ctr -a /run/containerd/containerd.sock -n k8s.io images import --no-unpack - \
        && echo "이미지 import 완료" \
        || { echo "이미지 import 실패"; exit 1; }
    fi

    # helm 설치 또는 업그레이드
    helm upgrade --install $RELEASE $CHART_DIR -n $NAMESPACE \
      --set api.image.repository="$IMAGE_REPO" \
      --set api.image.tag="$IMAGE_TAG" \
      --set api.env.dbName="$MYSQL_DATABASE" \
      --set api.secrets.dbPassword="$MYSQL_ROOT_PASSWORD" \
      --set api.secrets.redisPassword="$REDIS_PASSWORD" \
      --set api.secrets.jwtSecret="$JWT_SECRET" \
      --set api.secrets.kakaoClientId="$KAKAO_CLIENT_ID" \
      --set api.secrets.kakaoClientSecret="$KAKAO_CLIENT_SECRET" \
      --set api.secrets.naverClientId="$NAVER_CLIENT_ID" \
      --set api.secrets.naverClientSecret="$NAVER_CLIENT_SECRET" \
      --set api.secrets.awsAccessKey="$AWS_ACCESS_KEY" \
      --set api.secrets.awsSecretKey="$AWS_SECRET_KEY" \
      --set api.secrets.encryptionMasterKey="$ENCRYPTION_MASTER_KEY" \
      --set api.secrets.tossPaymentsSecretKey="$TOSS_PAYMENTS_SECRET_KEY" \
      --set api.secrets.internalApiKey="$INTERNAL_API_KEY"

    # Pod가 Ready 될 때까지 대기
    echo "Waiting for API pod to be ready..."
    kubectl wait --for=condition=ready pod -l app=$RELEASE-api -n $NAMESPACE --timeout=120s

    echo ""
    echo "=== Application deployed ==="
    echo "  API Server → localhost:30080"
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
    kubectl rollout status deployment/$RELEASE-api -n $NAMESPACE --timeout=120s
    echo "=== Application restarted ==="
    ;;

  logs)
    kubectl logs -f -l app=$RELEASE-api -n $NAMESPACE
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
