#!/bin/bash

DOCKER_IMAGE=$1
COMPOSE_FILE="/home/ec2-user/app/docker-compose.yml"
NGINX_CONF="/home/ec2-user/app/nginx.conf"

# 현재 실행 중인 앱 확인
CURRENT=$(docker ps --format '{{.Names}}' | grep -E 'app-(blue|green)' | head -1)

if [ "$CURRENT" == "app-blue" ]; then
    NEW="green"
    OLD="blue"
    NEW_PORT=8082
else
    NEW="blue"
    OLD="green"
    NEW_PORT=8081
fi

echo "현재: $OLD → 새로운: $NEW"

# 새 컨테이너 실행
DOCKER_IMAGE=$DOCKER_IMAGE docker-compose -f $COMPOSE_FILE --profile $NEW up -d app-$NEW

# 헬스체크 (최대 30초 대기)
echo "헬스체크 중..."
for i in {1..30}; do
    if curl -s http://localhost:$NEW_PORT/swagger-ui/index.html > /dev/null 2>&1; then
        echo "헬스체크 성공!"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "헬스체크 실패. 롤백."
        docker stop app-$NEW || true
        docker rm app-$NEW || true
        exit 1
    fi
    sleep 1
done

# Nginx 설정 변경
sed -i "s/app-$OLD/app-$NEW/g" $NGINX_CONF
docker exec nginx nginx -s reload

echo "Nginx 전환 완료: app-$NEW"

# 이전 컨테이너 종료
docker stop app-$OLD || true
docker rm app-$OLD || true

# 이미지 정리
docker image prune -f

echo "배포 완료!"