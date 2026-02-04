#!/bin/bash
#
# Modeunsa 인프라 관리 스크립트

# 사용하기 전 세팅:
#
#   1. Docker Desktop 설치
#      https://www.docker.com/products/docker-desktop/
#
#   2. Docker Desktop에서 Kubernetes 활성화
#      Docker Desktop > Settings > Kubernetes > Enable Kubernetes > Apply & Restart
#
#   3. kubectl 설치 (Docker Desktop K8s 활성화 시 자동 설치됨, 안 되어있으면 아래 실행)
#      Mac:     brew install kubectl
#      Windows: winget install Kubernetes.kubectl
#
#   4. helm 설치
#      Mac:     brew install helm
#      Windows: winget install Helm.Helm
#
#   * Windows에서는 IntelliJ 터미널을 Git Bash로 변경 후 이 스크립트 실행
#     IntelliJ > Settings > Tools > Terminal > Shell path를 "C:\Program Files\Git\bin\bash.exe"로 변경
#
# !!!!!!!!!!!!!!!!!!!!!!사용법!!!!!!!!!!!!!!!!!!!!!!
#   ./k8s/infra.sh up        인프라 시작 (helm 설치)
#   ./k8s/infra.sh down      인프라 중지 (데이터 유지)
#   ./k8s/infra.sh clean     인프라 중지 + 데이터 삭제 (PVC 포함 전체 삭제)
#   ./k8s/infra.sh status    인프라 상태 확인
#   ./k8s/infra.sh restart   인프라 재시작
#
#
# 접속 정보:
#   MySQL          localhost:30306 (NodePort)
#   Redis          localhost:30379 (NodePort)
#   Elasticsearch  localhost:30920 (NodePort)
#   Prometheus     localhost:30090 (NodePort)
#   Grafana        localhost:30300 (NodePort)
#

NAMESPACE="modeunsa"
RELEASE="modeunsa-infra"
CHART_DIR="$(dirname "$0")/infra"
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

    # namespace 생성 (이미 있으면 무시)
    kubectl create namespace $NAMESPACE 2>/dev/null

    # helm 설치 또는 업그레이드 (서브차트 구조 + .env에서 비밀값 주입)
    helm upgrade --install $RELEASE $CHART_DIR -n $NAMESPACE \
      --set mysql.auth.rootPassword="$MYSQL_ROOT_PASSWORD" \
      --set mysql.auth.database="$MYSQL_DATABASE" \
      --set redis.auth.password="$REDIS_PASSWORD" \
      --set grafana.auth.adminUser="$GF_SECURITY_ADMIN_USER" \
      --set grafana.auth.adminPassword="$GF_SECURITY_ADMIN_PASSWORD"

    # Pod가 Ready 될 때까지 대기 (Pod 생성까지 기다린 후 Ready 확인)
    echo "Waiting for pods to be ready..."
    for APP in mysql redis elasticsearch; do
      echo -n "  $APP: "
      # Pod가 생성될 때까지 대기
      while ! kubectl get pod -l app=$RELEASE-$APP -n $NAMESPACE 2>/dev/null | grep -q "$RELEASE-$APP"; do
        sleep 1
      done
      # Pod가 Ready 될 때까지 대기
      kubectl wait --for=condition=ready pod -l app=$RELEASE-$APP -n $NAMESPACE --timeout=120s
    done

    echo ""
    echo "=== Infrastructure started ==="
    echo "  MySQL          → localhost:30306"
    echo "  Redis          → localhost:30379"
    echo "  Elasticsearch  → localhost:30920"
    echo "  Prometheus     → localhost:30090"
    echo "  Grafana        → localhost:30300"
    ;;

  down)
    # helm 삭제 (PVC는 유지 → 데이터 보존)
    helm uninstall $RELEASE -n $NAMESPACE 2>/dev/null

    echo "=== Infrastructure stopped (데이터 유지됨) ==="
    ;;

  clean)
    # helm 삭제
    helm uninstall $RELEASE -n $NAMESPACE 2>/dev/null

    # PVC 삭제 (데이터 완전 삭제)
    kubectl delete pvc --all -n $NAMESPACE 2>/dev/null

    echo "=== Infrastructure stopped (데이터 삭제됨) ==="
    ;;

  status)
    kubectl get all,pvc -n $NAMESPACE
    ;;

  restart)
    $0 down
    sleep 2
    $0 up
    ;;

  *)
    echo "Usage: $0 {up|down|clean|status|restart}"
    ;;
esac
