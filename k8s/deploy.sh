#!/bin/bash
#
# Modeunsa 통합 배포 스크립트
#
# 사용법:
#   ./k8s/deploy.sh up [dev|prod]     인프라 + 앱 전체 시작
#   ./k8s/deploy.sh down              인프라 + 앱 전체 중지 (데이터 유지)
#   ./k8s/deploy.sh clean             인프라 + 앱 전체 삭제 (데이터 포함)
#   ./k8s/deploy.sh status            전체 상태 확인
#   ./k8s/deploy.sh restart [dev|prod] 전체 재시작
#
#   ./k8s/deploy.sh infra [dev|prod]  인프라만 시작
#   ./k8s/deploy.sh app [dev|prod]    앱만 시작 (인프라가 실행 중이어야 함)
#
# 접속 정보:
#   API Server     localhost:30080
#   MySQL          localhost:30306
#   Redis          localhost:30379
#   Elasticsearch  localhost:30920
#   Prometheus     localhost:30090
#   Grafana        localhost:30300
#

SCRIPT_DIR="$(dirname "$0")"

print_status() {
    echo ""
    echo "==========================================="
    echo "  $1"
    echo "==========================================="
    echo ""
}

case "$1" in
  up)
    print_status "Starting Infrastructure..."
    "$SCRIPT_DIR/infra.sh" up "$2" || exit 1

    print_status "Starting Application..."
    "$SCRIPT_DIR/app.sh" up "$2" || exit 1
    
    print_status "All services started!"
    echo "  API Server     → localhost:30080"
    echo "  MySQL          → localhost:30306"
    echo "  Redis          → localhost:30379"
    echo "  Elasticsearch  → localhost:30920"
    echo "  Prometheus     → localhost:30090"
    echo "  Grafana        → localhost:30300"
    ;;

  down)
    print_status "Stopping Application..."
    "$SCRIPT_DIR/app.sh" down
    
    print_status "Stopping Infrastructure..."
    "$SCRIPT_DIR/infra.sh" down
    
    print_status "All services stopped (데이터 유지됨)"
    ;;

  clean)
    print_status "Stopping Application..."
    "$SCRIPT_DIR/app.sh" down
    
    print_status "Cleaning Infrastructure (데이터 삭제)..."
    "$SCRIPT_DIR/infra.sh" clean
    
    print_status "All services stopped (데이터 삭제됨)"
    ;;

  status)
    NS="modeunsa"
    case "$2" in
      pod|pods|"")  kubectl get pods -n $NS ;;
      deploy*)      kubectl get deployments -n $NS ;;
      sts|statefulset*) kubectl get statefulsets -n $NS ;;
      svc|service*) kubectl get svc -n $NS ;;
      pvc)          kubectl get pvc -n $NS ;;
      all)          kubectl get all,pvc -n $NS ;;
      *)            echo "Usage: $0 status [pod|deploy|sts|svc|pvc|all]" ;;
    esac
    ;;

  restart)
    print_status "Restarting All Services..."
    "$SCRIPT_DIR/app.sh" down
    "$SCRIPT_DIR/infra.sh" down
    sleep 2
    "$SCRIPT_DIR/infra.sh" up "$2"
    "$SCRIPT_DIR/app.sh" up "$2"

    print_status "All services restarted!"
    ;;

  infra)
    print_status "Starting Infrastructure only..."
    "$SCRIPT_DIR/infra.sh" up "$2"
    ;;

  app)
    print_status "Starting Application only..."
    "$SCRIPT_DIR/app.sh" up "$2"
    ;;

  *)
    echo "Modeunsa Deploy Script"
    echo ""
    echo "Usage: $0 {up|down|clean|status|restart|infra|app} [dev|prod]"
    echo ""
    echo "Commands:"
    echo "  up [dev|prod]       인프라 + 앱 전체 시작"
    echo "  down                인프라 + 앱 전체 중지 (데이터 유지)"
    echo "  clean               인프라 + 앱 전체 삭제 (데이터 포함)"
    echo "  status              전체 상태 확인"
    echo "  restart [dev|prod]  전체 재시작"
    echo "  infra [dev|prod]    인프라만 시작"
    echo "  app [dev|prod]      앱만 시작"
    ;;
esac
