#!/bin/bash
set -e

DOCKER_IMAGE=$1
COMPOSE_FILE="/home/ec2-user/app/docker-compose.yml"
APP_DIR="/home/ec2-user/app"
LOCK_FILE="/tmp/deploy.lock"

# 락 파일로 동시 실행 방지
if [ -f "$LOCK_FILE" ]; then
    echo "다른 배포가 진행 중입니다. 잠시 후 다시 시도해주세요."
    exit 1
fi
trap "rm -f $LOCK_FILE" EXIT
touch $LOCK_FILE

# Docker 이미지 환경변수 설정
export DOCKER_IMAGE=$DOCKER_IMAGE

# 네트워크 생성 (없으면)
docker network create modeunsa-net 2>/dev/null || true

# Redis 실행 (docker-compose로 관리)
if ! docker ps | grep -q redis; then
    echo "Redis 컨테이너 시작..."
    docker-compose -f $COMPOSE_FILE up -d redis
fi

# 현재 실행 중인 앱 확인
CURRENT=$(docker ps --format '{{.Names}}' | grep -E 'app-(blue|green)' | head -1)

if [ -z "$CURRENT" ]; then
    NEW="blue"
    OLD=""
    NEW_PORT=8081
    echo "첫 배포: app-blue로 시작"
elif [ "$CURRENT" == "app-blue" ]; then
    NEW="green"
    OLD="blue"
    NEW_PORT=8082
    echo "현재: blue → 새로운: green"
else
    NEW="blue"
    OLD="green"
    NEW_PORT=8081
    echo "현재: green → 새로운: blue"
fi

# 새 컨테이너 실행
echo "새 컨테이너 실행: app-$NEW"
docker-compose -f $COMPOSE_FILE --profile $NEW up -d app-$NEW

# 헬스체크 (최대 60초 대기)
echo "헬스체크 중..."
for i in {1..60}; do
    if curl -s http://localhost:$NEW_PORT/actuator/health | grep -q '"status":"UP"'; then
        echo "헬스체크 성공!"
        break
    fi
    if [ $i -eq 60 ]; then
        echo "헬스체크 실패. 롤백."
        docker stop app-$NEW || true
        docker rm app-$NEW || true
        exit 1
    fi
    sleep 1
done

# JVM Warm-up (5회 요청)
echo "JVM Warm-up 중..."
for i in {1..5}; do
    curl -s http://localhost:$NEW_PORT/actuator/health > /dev/null 2>&1
    sleep 1
done
echo "Warm-up 완료!"

# Nginx 실행 확인 및 설정 교체
if ! docker ps | grep -q nginx; then
    echo "Nginx 컨테이너 시작..."
    cp $APP_DIR/nginx-$NEW.conf $APP_DIR/nginx.conf
    docker run -d --name nginx --network modeunsa-net -p 80:80 -v $APP_DIR/nginx.conf:/etc/nginx/nginx.conf nginx:alpine
else
    cp $APP_DIR/nginx-$NEW.conf $APP_DIR/nginx.conf
    docker cp $APP_DIR/nginx.conf nginx:/etc/nginx/nginx.conf
    docker exec nginx nginx -s reload
fi

echo "Nginx 전환 완료: app-$NEW"

# 이전 컨테이너 종료 (첫 배포가 아닌 경우만)
if [ -n "$OLD" ]; then
    echo "이전 컨테이너 종료: app-$OLD"
    docker stop app-$OLD || true
    docker rm app-$OLD || true
fi

# 이미지 정리
docker image prune -f

echo "배포 완료!"