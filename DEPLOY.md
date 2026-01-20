# 배포 가이드

## 아키텍처
```
사용자 → Nginx (80) → app-blue (8081) / app-green (8082) → Redis (6379)
```

## CI/CD 흐름
```
┌─────────────────────────────────────────────────────────────────┐
│                        GitHub Actions                           │
├─────────────────────────────────────────────────────────────────┤
│  1. main 브랜치 push                                             │
│              ↓                                                  │
│  2. JDK 21 설정 & Gradle 빌드                                    │
│              ↓                                                  │
│  3. Docker 이미지 빌드 & Docker Hub push                          │
│              ↓                                                  │
│  4. EC2로 설정 파일 전송 (docker-compose, nginx.conf, deploy.sh)   │
│              ↓                                                  │
│  5. deploy.sh 실행 (Blue-Green 배포)                             │
└─────────────────────────────────────────────────────────────────┘
```

## 무중단 배포 (Blue-Green)
```
1. 현재 app-blue 실행 중
          ↓
2. app-green 새 버전으로 실행
          ↓
3. 헬스체크 (최대 30초)
          ↓
4. 성공 시 Nginx가 app-green으로 전환
          ↓
5. app-blue 종료
```

### 롤백 전략

- 헬스체크 실패 시 자동 롤백
- 새 컨테이너 종료 후 기존 컨테이너 유지

## 인프라 구성

| 서비스 | 사양 |
|--------|------|
| EC2 | t3.large, Amazon Linux 2023, 50GB gp3 |
| Docker | nginx:alpine, redis:7-alpine |
| 리전 | ap-northeast-2 (서울) |

## 환경변수

### GitHub Secrets

| Name | 설명 |
|------|------|
| `DOCKER_USERNAME` | Docker Hub 아이디 |
| `DOCKER_PASSWORD` | Docker Hub Access Token |
| `EC2_HOST` | EC2 Elastic IP |
| `EC2_KEY` | EC2 pem 키 내용 |

### 애플리케이션 환경변수

| Name | 설명 | 기본값 |
|------|------|--------|
| `REDIS_HOST` | Redis 호스트 | localhost |
| `REDIS_PORT` | Redis 포트 | 6379 |

## 로컬에서 수동 배포
```bash
# EC2 접속
ssh -i modeunsa_pem.pem ec2-user@<EC2_IP>

# 앱 디렉토리 이동
cd /home/ec2-user/app

# 수동 배포
./deploy.sh <DOCKER_USERNAME>/modeunsa:latest
```

## 컨테이너 관리
```bash
# 상태 확인
docker ps

# 로그 확인
docker logs app-blue
docker logs app-green
docker logs nginx

# 컨테이너 재시작
docker restart nginx
```

## 포트 구성

| 포트 | 서비스 |
|------|--------|
| 80 | Nginx (외부 접근) |
| 8081 | app-blue |
| 8082 | app-green |
| 6379 | Redis |