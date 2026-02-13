#!/bin/bash
#
# Modeunsa 인프라 관리 스크립트 (k3s)

# 사용하기 전 세팅:
#
#   [macOS / dev 환경]
#   1. Colima 및 Docker CLI 설치
#      brew install colima docker
#
#   2. kubectl, helm 설치
#      brew install kubectl helm
#
#   * Colima(k3s)는 infra.sh up 시 자동 시작됩니다.
#   * Docker Desktop은 불필요합니다.
#
#   [Linux / prod 환경]
#   1. k3s 설치
#      curl -sfL https://get.k3s.io | sh -
#
#   2. kubectl, helm 설치
#      sudo apt install kubectl helm (또는 공식 설치 스크립트)
#
#   * k3s가 미리 실행 중이어야 합니다.
#
# !!!!!!!!!!!!!!!!!!!!!!사용법!!!!!!!!!!!!!!!!!!!!!!
#   ./k8s/infra.sh up [dev|prod]    인프라 시작 (기본: dev)
#   ./k8s/infra.sh down             인프라 중지 (데이터 유지)
#   ./k8s/infra.sh clean            인프라 중지 + 데이터 삭제 (PVC 포함 전체 삭제)
#   ./k8s/infra.sh status           인프라 상태 확인
#   ./k8s/infra.sh restart [dev|prod] 인프라 재시작
#
#
# 접속 정보 (dev - NodePort):
#   MySQL          localhost:30306 (NodePort)
#   Redis          localhost:30379 (NodePort)
#   Elasticsearch  localhost:30920 (NodePort)
#   Prometheus     localhost:30090 (NodePort)
#   Grafana        localhost:30300 (NodePort)
#   Kafka          localhost:30092 (NodePort)
#   Kafka-UI       localhost:30085 (NodePort)
#
# 접속 정보 (prod - Ingress):
#   Grafana        <EC2-IP>/grafana
#   Prometheus     <EC2-IP>/prometheus
#

NAMESPACE="modeunsa"
RELEASE="modeunsa-infra"
CHART_DIR="$(dirname "$0")/infra"
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

ensure_colima() {
  # colima status 종료 코드로 판단 (0=실행중, 1=중지)
  if ! colima status &>/dev/null; then
    echo "Colima가 실행 중이 아닙니다. k3s 모드로 시작합니다..."
    colima start --kubernetes \
      --vz-rosetta \
      --cpu 2 --memory 7 --disk 30 || { echo "Colima 시작 실패"; exit 1; }
  fi

  # k8s 클러스터가 완전히 준비될 때까지 대기 (coredns 기준)
  echo "Kubernetes 클러스터 준비 대기..."
  until kubectl wait --for=condition=ready pod -l k8s-app=kube-dns -n kube-system --timeout=120s &>/dev/null; do
    sleep 3
  done
  echo "Kubernetes 클러스터 준비 완료"
}

ensure_k3s() {
  # k3s가 실행 중인지 확인 (systemd 기반)
  if ! systemctl is-active --quiet k3s 2>/dev/null; then
    echo "k3s가 실행 중이 아닙니다. 먼저 k3s를 시작하세요."
    echo "  sudo systemctl start k3s"
    exit 1
  fi

  # kubeconfig 설정 확인
  if [ ! -f /etc/rancher/k3s/k3s.yaml ]; then
    echo "k3s kubeconfig를 찾을 수 없습니다: /etc/rancher/k3s/k3s.yaml"
    exit 1
  fi

  # KUBECONFIG 환경변수 설정 (필요시)
  export KUBECONFIG=/etc/rancher/k3s/k3s.yaml

  echo "k3s 클러스터 준비 확인..."
  kubectl wait --for=condition=ready pod -l k8s-app=kube-dns -n kube-system --timeout=120s &>/dev/null \
    || { echo "k3s 클러스터가 준비되지 않았습니다."; exit 1; }
  echo "k3s 클러스터 준비 완료"
}

case "$1" in
  up)
    ENV="${2:-dev}"  # 기본값 dev
    ENV_FILE=$(get_env_file "$ENV")

    # OS 감지: macOS는 Colima, Linux는 k3s
    if [[ "$(uname)" == "Darwin" ]]; then
      ensure_colima
    else
      ensure_k3s
    fi

    if [ "$ENV" = "dev" ]; then
      VALUES_FILES=""  # values.yaml만 사용 (기본값)
      echo "=== dev 환경으로 시작합니다 (NodePort) ==="
    elif [ "$ENV" = "prod" ]; then
      VALUES_FILES="-f $CHART_DIR/values-prod.yaml"  # prod 설정으로 덮어쓰기
      echo "=== prod 환경으로 시작합니다 (ClusterIP + Ingress) ==="
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

    # namespace 생성 (이미 있으면 무시)
    kubectl create namespace $NAMESPACE 2>/dev/null

    # helm 설치 또는 업그레이드 (서브차트 구조 + .env에서 비밀값 주입)
    helm upgrade --install $RELEASE $CHART_DIR -n $NAMESPACE $VALUES_FILES \
      --set mysql.auth.rootPassword="$MYSQL_ROOT_PASSWORD" \
      --set mysql.auth.database="$MYSQL_DATABASE" \
      --set mysql-exporter.mysql.password="$MYSQL_ROOT_PASSWORD" \
      --set redis.auth.password="$REDIS_PASSWORD" \
      --set grafana.auth.adminUser="$GF_SECURITY_ADMIN_USER" \
      --set grafana.auth.adminPassword="$GF_SECURITY_ADMIN_PASSWORD" \
      --set cert-manager.email="$CERT_MANAGER_EMAIL"

    # Pod가 Ready 될 때까지 대기 (Pod 생성까지 기다린 후 Ready 확인)
    echo "Waiting for pods to be ready..."
    for APP in mysql redis; do
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
    if [ "$ENV" = "dev" ]; then
      echo "  MySQL          → localhost:30306"
      echo "  Redis          → localhost:30379"
      echo "  Elasticsearch  → localhost:30920"
      echo "  Prometheus     → localhost:30090"
      echo "  Grafana        → localhost:30300"
      echo "  Kafka          → localhost:30092"
      echo "  Kafka-UI       → localhost:30085"
    else
      echo "  서비스들이 ClusterIP로 실행됩니다."
      echo "  Grafana        → <EC2-IP>/grafana (Ingress)"
      echo "  Prometheus     → <EC2-IP>/prometheus (Ingress)"
    fi
    ;;

  down)
    # helm 삭제 (PVC는 유지 → 데이터 보존)
    helm uninstall $RELEASE -n $NAMESPACE 2>/dev/null

    # dev 환경에서만 colima 중지
    if colima status &>/dev/null; then
      colima stop 2>/dev/null
    fi

    echo "=== Infrastructure stopped (데이터 유지됨) ==="
    ;;

  clean)
    # helm 삭제
    helm uninstall $RELEASE -n $NAMESPACE 2>/dev/null

    # PVC 삭제 (데이터 완전 삭제)
    kubectl delete pvc --all -n $NAMESPACE 2>/dev/null

    # dev 환경에서만 colima 중지
    if colima status &>/dev/null; then
      colima stop 2>/dev/null
    fi

    echo "=== Infrastructure stopped (데이터 삭제됨) ==="
    ;;

  status)
    case "$2" in
      pod|pods|"")  kubectl get pods -n $NAMESPACE ;;
      deploy*)      kubectl get deployments -n $NAMESPACE ;;
      sts|statefulset*) kubectl get statefulsets -n $NAMESPACE ;;
      svc|service*) kubectl get svc -n $NAMESPACE ;;
      pvc)          kubectl get pvc -n $NAMESPACE ;;
      all)          kubectl get all,pvc -n $NAMESPACE ;;
      *)            echo "Usage: $0 status [pod|deploy|sts|svc|pvc|all]" ;;
    esac
    ;;

  restart)
    $0 down
    sleep 2
    $0 up "${2:-dev}"
    ;;

  *)
    echo "Usage: $0 {up|down|clean|status|restart} [dev|prod]"
    echo ""
    echo "Examples:"
    echo "  $0 up dev       # dev 환경으로 시작"
    echo "  $0 up prod      # prod 환경으로 시작"
    echo "  $0 down         # 중지"
    echo "  $0 status       # 상태 확인"
    ;;
esac
