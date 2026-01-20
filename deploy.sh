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

# Redis, MySQL 컨테이너 확인 및 실행
echo "Redis, MySQL 컨테이너 확인..."
docker-compose -f $COMPOSE_FILE up -d redis mysql

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
docker-compose -f $COMPOSE_FILE --profile $NEW pull app-$NEW
docker-compose -f $COMPOSE_FILE --profile $NEW up -d --force-recreate app-$NEW

# 애플리케이션 구동 대기
echo "애플리케이션 구동을 위해 30초 대기합니다..."
sleep 30

# 헬스체크 (1초 간격으로 10회 추가 시도)
echo "헬스체크 시작..."
for i in {1..10}; do
    # curl 실패 시 스크립트 종료 방지를 위해 || true 추가
    RESPONSE=$(curl -s http://localhost:$NEW_PORT/actuator/health/readiness || true)
    UP_COUNT=$(echo $RESPONSE | grep -c '"status":"UP"')

    if [ $UP_COUNT -ge 1 ]; then
        echo "헬스체크 성공!"
        break
    else
        echo "헬스체크 응답 없음 또는 실패. 재시도 중... ($i/10)"
    fi

    if [ $i -eq 10 ]; then
        echo "헬스체크 최종 실패. 롤백을 수행합니다."
        echo "실패 로그 확인:"
        docker logs --tail 50 app-$NEW

        docker stop app-$NEW || true
        docker rm app-$NEW || true
        exit 1
    fi
    sleep 3 # 재시도 간격 3초
done

# JVM Warm-up (5회 요청)
echo "JVM Warm-up 중..."
for i in {1..5}; do
    curl -s http://localhost:$NEW_PORT/actuator/health > /dev/null 2>&1
    sleep 1
done
echo "Warm-up 완료!"

# Nginx 설정 교체 및 검증 로직 개선
NGINX_CONTAINER="nginx"
NEW_CONF="$APP_DIR/nginx-$NEW.conf"

echo "Nginx 설정 전환 준비 중..."

# Nginx 컨테이너 실행 여부 확인
if [ "$(docker ps -q -f name=^/${NGINX_CONTAINER}$)" ]; then
    echo "Nginx가 실행 중입니다. 설정 검증을 시작합니다."

    # 1. 검증을 위해 임시 경로로 설정 파일 복사
    docker cp "$NEW_CONF" "${NGINX_CONTAINER}:/etc/nginx/nginx.conf.test"

    # 2. nginx -t 명령어로 설정 파일 유효성 검사 (테스트 파일 사용)
    if docker exec "$NGINX_CONTAINER" nginx -t -c /etc/nginx/nginx.conf.test; then
        echo "Nginx 설정 문법 검사 통과."

        # 3. 호스트 파일 변경
        cp "$NEW_CONF" "$APP_DIR/nginx.conf"

        # 4. 설정 재로딩 (HUP 시그널 전송 방식 사용)
        if docker kill -s HUP "$NGINX_CONTAINER"; then
            echo "Nginx 재로딩(HUP) 완료."
            # 호스트의 메인 설정 파일도 최신화 (참고용)
            cp "$NEW_CONF" "$APP_DIR/nginx.conf"
        else
            echo "❌ Nginx 재로딩 실패! (프로세스 오류)"
            # reload 실패 시 롤백 수행
            docker stop "app-$NEW"
            docker rm "app-$NEW"
            exit 1
        fi
    else
        echo "❌ Nginx 설정 문법 오류 발견! 배포를 중단합니다."
        # 문법 오류 상세 출력
        docker exec "$NGINX_CONTAINER" nginx -t -c /etc/nginx/nginx.conf.test || true

        # 롤백 수행 (새로 띄운 앱 컨테이너 종료)
        docker stop "app-$NEW"
        docker rm "app-$NEW"
        exit 1
    fi

else
    # Nginx가 실행 중이 아니면 (최초 실행 또는 정지 상태)
    echo "Nginx가 실행 중이 아닙니다. 컨테이너를 새로 시작합니다."

    # 기존에 멈춰있는 컨테이너가 있다면 삭제
    if [ "$(docker ps -aq -f name=^/${NGINX_CONTAINER}$)" ]; then
        docker rm -f "$NGINX_CONTAINER"
    fi

    # 호스트 설정 파일 최신화
    cp "$NEW_CONF" "$APP_DIR/nginx.conf"

    # Nginx 컨테이너 실행
    docker run -d --name "$NGINX_CONTAINER" \
        --network modeunsa-net \
        -p 80:80 \
        -v "$APP_DIR/nginx.conf:/etc/nginx/nginx.conf" \
        nginx:alpine

    echo "Nginx 컨테이너 시작 완료."
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