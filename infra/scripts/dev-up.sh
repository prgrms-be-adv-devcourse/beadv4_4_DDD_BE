#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="$SCRIPT_DIR/../docker"
ENV_FILE="$SCRIPT_DIR/../env/.env.local"

# 환경변수 파일 확인
if [ ! -f "$ENV_FILE" ]; then
    echo "❌ .env.local 파일이 없습니다."
    echo "   cp env/.env.example env/.env.local 후 값을 설정해주세요."
    exit 1
fi

case "${1:-infra}" in
    "infra")
        echo "🚀 인프라만 실행 (MySQL, Redis, ES)"
        docker-compose -f "$DOCKER_DIR/docker-compose.yml" \
                       --env-file "$ENV_FILE" \
                       up -d
        echo ""
        echo "✅ 인프라 실행 완료!"
        echo "   MySQL: localhost:3306"
        echo "   Redis: localhost:6379"
        echo "   Elasticsearch: localhost:9200"
        echo ""
        echo "💡 앱은 IDE에서 'local' 프로파일로 실행하세요."
        echo "   SPRING_PROFILES_ACTIVE=local"
        ;;
    "all")
        echo "🚀 인프라 + 앱 컨테이너 실행"
        docker-compose -f "$DOCKER_DIR/docker-compose.yml" \
                       -f "$DOCKER_DIR/docker-compose.local.yml" \
                       --env-file "$ENV_FILE" \
                       --profile with-app \
                       up -d --build
        echo ""
        echo "✅ 전체 실행 완료!"
        echo "   App: localhost:8080"
        ;;
    "down")
        echo "🛑 모든 컨테이너 종료"
        docker-compose -f "$DOCKER_DIR/docker-compose.yml" \
                       -f "$DOCKER_DIR/docker-compose.local.yml" \
                       --env-file "$ENV_FILE" \
                       down
        ;;
    "clean")
        echo "🧹 컨테이너 + 볼륨 삭제 (데이터 초기화)"
        docker-compose -f "$DOCKER_DIR/docker-compose.yml" \
                       -f "$DOCKER_DIR/docker-compose.local.yml" \
                       --env-file "$ENV_FILE" \
                       down -v
        ;;
    *)
        echo "Usage: $0 {infra|all|down|clean}"
        echo "  infra : MySQL, Redis, ES만 실행 (기본값)"
        echo "  all   : 인프라 + 앱 컨테이너 실행"
        echo "  down  : 모든 컨테이너 종료"
        echo "  clean : 컨테이너 + 볼륨 삭제"
        exit 1
        ;;
esac