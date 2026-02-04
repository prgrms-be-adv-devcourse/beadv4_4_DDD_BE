#!/bin/bash
#
# Modeunsa 애플리케이션 배포 스크립트
#
# 사전 조건:
#   - infra가 먼저 실행되어 있어야 함 (./k8s/infra.sh up)
#   - Docker 이미지가 빌드되어 있어야 함
#
# 사용법:
#   ./k8s/app.sh up        앱 배포
#   ./k8s/app.sh down      앱 중지
#   ./k8s/app.sh status    앱 상태 확인
#   ./k8s/app.sh restart   앱 재시작
#   ./k8s/app.sh logs      앱 로그 확인
#
# 접속 정보:
#   API Server    localhost:30080
#

NAMESPACE="modeunsa"
RELEASE="modeunsa-app"
CHART_DIR="$(dirname "$0")/app"
ENV_FILE="$(dirname "$0")/../.env"

case "$1" in
  up)
    # .env 로드
    if [ -f "$ENV_FILE" ]; then
      source "$ENV_FILE"
    else
      echo ".env 파일을 찾을 수 없습니다: $ENV_FILE"
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
      --set api.secrets.tossPaymentsSecretKey="$TOSS_PAYMENTS_SECRET_KEY"

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
    echo "=== Pods ==="
    kubectl get pods -n $NAMESPACE -l "app.kubernetes.io/instance=$RELEASE"
    echo ""
    echo "=== Services ==="
    kubectl get svc -n $NAMESPACE -l "app.kubernetes.io/instance=$RELEASE"
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
    echo "Usage: $0 {up|down|status|restart|logs}"
    ;;
esac
