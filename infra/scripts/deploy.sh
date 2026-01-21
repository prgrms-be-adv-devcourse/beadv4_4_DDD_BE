#!/bin/bash
set -e

DOCKER_IMAGE=$1
APP_DIR="/home/ec2-user/app"
COMPOSE_FILE="$APP_DIR/docker/docker-compose.yml"
COMPOSE_PROD="$APP_DIR/docker/docker-compose.prod.yml"
ENV_FILE="$APP_DIR/env/.env.prod"
NGINX_DIR="$APP_DIR/nginx"
LOCK_FILE="/tmp/deploy.lock"

# 락 파일로 동시 실행 방지
if [ -f "$LOCK_FILE" ]; then
    echo "다른 배포가 진행 중입니다. 잠시 후 다시 시도해주세요."
    exit 1
fi
trap "rm -f $LOCK_FILE" EXIT
touch $LOCK_FILE

# 환경변수 파일 확인
if [ ! -f "$ENV_FILE" ]; then
    echo "❌ .env.prod 파일이 없습니다: $ENV_FILE"
    exit 1
fi

# Docker 이미지 환경변수 설정
export DOCKER_IMAGE=$DOCKER_IMAGE

# 네트워크 생성 (없으면)
docker network create modeunsa-net 2>/dev/null || true

# 인프라 컨테이너 확인 및 실행 (MySQL, Redis, ES)
echo "📦 인프라 컨테이너 확인..."
docker-compose -f $COMPOSE_FILE --env-file $ENV_FILE up -d mysql redis elasticsearch

# MySQL 준비 대기
echo "⏳ MySQL 준비 대기 중..."
for i in {1..30}; do
    if docker exec modeunsa-mysql mysqladmin ping -h localhost -u root -p"${DB_PASSWORD}" --silent 2>/dev/null; then
        echo "✅ MySQL 준비 완료"
        break
    fi
    echo "MySQL 대기 중... ($i/30)"
    sleep 2
done

# 현재 실행 중인 앱 확인
CURRENT=$(docker ps --format '{{.Names}}' | grep -E 'app-(blue|green)' | head -1)

if [ -z "$CURRENT" ]; then
    NEW="blue"
    OLD=""
    NEW_PORT=8081
    echo "🚀 첫 배포: app-blue로 시작"
elif [ "$CURRENT" == "app-blue" ]; then
    NEW="green"
    OLD="blue"
    NEW_PORT=8082
    echo "🔄 현재: blue → 새로운: green"
else
    NEW="blue"
    OLD="green"
    NEW_PORT=8081
    echo "🔄 현재: green → 새로운: blue"
fi

# 새 컨테이너 실행
echo "🐳 새 컨테이너 실행: app-$NEW"
docker-compose -f $COMPOSE_FILE -f $COMPOSE_PROD \
    --env-file $ENV_FILE \
    --profile $NEW pull app-$NEW

docker-compose -f $COMPOSE_FILE -f $COMPOSE_PROD \
    --env-file $ENV_FILE \
    --profile $NEW up -d --force-recreate app-$NEW

# 애플리케이션 구동 대기
echo "⏳ 애플리케이션 구동을 위해 30초 대기합니다..."
sleep 30

# 헬스체크 (3초 간격으로 10회 시도)
echo "🏥 헬스체크 시작..."
for i in {1..10}; do
    RESPONSE=$(curl -s http://localhost:$NEW_PORT/actuator/health/readiness || true)
    UP_COUNT=$(echo $RESPONSE | grep -c '"status":"UP"' || echo "0")

    if [ "$UP_COUNT" -ge 1 ]; then
        echo "✅ 헬스체크 성공!"
        break
    else
        echo "헬스체크 재시도 중... ($i/10)"
    fi

    if [ $i -eq 10 ]; then
        echo "❌ 헬스체크 최종 실패. 롤백을 수행합니다."
        echo "실패 로그:"
        docker logs --tail 50 app-$NEW
        docker stop app-$NEW || true
        docker rm app-$NEW || true
        exit 1
    fi
    sleep 3
done

# JVM Warm-up
echo "🔥 JVM Warm-up 중..."
for i in {1..5}; do
    curl -s http://localhost:$NEW_PORT/actuator/health > /dev/null 2>&1
    sleep 1
done
echo "Warm-up 완료!"

# Nginx 설정 교체
NGINX_CONTAINER="nginx"
NEW_CONF="$NGINX_DIR/nginx-$NEW.conf"

echo "🔧 Nginx 설정 전환 준비 중..."

if [ "$(docker ps -q -f name=^/${NGINX_CONTAINER}$)" ]; then
    echo "Nginx가 실행 중입니다. 설정 검증을 시작합니다."

    docker cp "$NEW_CONF" "${NGINX_CONTAINER}:/etc/nginx/nginx.conf.test"

    if docker exec "$NGINX_CONTAINER" nginx -t -c /etc/nginx/nginx.conf.test; then
        echo "✅ Nginx 설정 문법 검사 통과."
        cp "$NEW_CONF" "$NGINX_DIR/nginx.conf"

        if docker kill -s HUP "$NGINX_CONTAINER"; then
            echo "✅ Nginx 재로딩(HUP) 완료."
        else
            echo "❌ Nginx 재로딩 실패!"
            docker stop "app-$NEW"
            docker rm "app-$NEW"
            exit 1
        fi
    else
        echo "❌ Nginx 설정 문법 오류 발견!"
        docker exec "$NGINX_CONTAINER" nginx -t -c /etc/nginx/nginx.conf.test || true
        docker stop "app-$NEW"
        docker rm "app-$NEW"
        exit 1
    fi
else
    echo "Nginx가 실행 중이 아닙니다. 컨테이너를 새로 시작합니다."

    if [ "$(docker ps -aq -f name=^/${NGINX_CONTAINER}$)" ]; then
        docker rm -f "$NGINX_CONTAINER"
    fi

    cp "$NEW_CONF" "$NGINX_DIR/nginx.conf"

    docker-compose -f $COMPOSE_FILE -f $COMPOSE_PROD \
        --env-file $ENV_FILE up -d nginx

    echo "✅ Nginx 컨테이너 시작 완료."
fi

echo "✅ Nginx 전환 완료: app-$NEW"

# 이전 컨테이너 종료
if [ -n "$OLD" ]; then
    echo "🛑 이전 컨테이너 종료: app-$OLD"
    docker stop app-$OLD || true
    docker rm app-$OLD || true
fi

# 이미지 정리
docker image prune -f

echo "🎉 배포 완료!"