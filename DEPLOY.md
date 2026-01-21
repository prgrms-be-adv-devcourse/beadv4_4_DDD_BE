# 배포 가이드

## 아키텍처
```
사용자 → Nginx (80) → app-blue (8081) / app-green (8082)
                              ↓
                      ┌───────┴───────┐
                      ↓               ↓
               MySQL (3306)    Redis (6379)
                      ↓
              Elasticsearch (9200)
```

## 디렉토리 구조
```
/home/ec2-user/app/
├── config/
│   └── env/
│       └── .env.prod              # 프로덕션 환경변수 (서버에서 직접 관리)
├── infra/
│   ├── docker/
│   │   ├── docker-compose.yml
│   │   ├── docker-compose-dev.yml
│   │   ├── docker-compose-prod.yml
│   │   ├── backend/
│   │   └── elasticsearch/
│   ├── nginx/
│   │   ├── nginx-blue.conf
│   │   └── nginx-green.conf
│   ├── scripts/
│   │   ├── deploy.sh
│   │   └── dev-up.sh
│   └── systemd/
│       └── modeunsa.service
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
│  3. Docker 이미지 빌드 & Docker Hub push                         │
│     (태그: YYYYMMDD-RUN_NUMBER, latest)                         │
│              ↓                                                  │
│  4. EC2로 설정 파일 전송 (SCP)                                    │
│     - infra/ → /home/ec2-user/app/infra/                        │
│              ↓                                                  │
│  5. deploy.sh 실행 (Blue-Green 배포)                             │
└─────────────────────────────────────────────────────────────────┘
```

## 무중단 배포 (Blue-Green)
```
1. 인프라 컨테이너 확인 (MySQL, Redis, ES)
          ↓
2. 현재 실행 중인 앱 확인 (blue/green)
          ↓
3. 새 컨테이너 실행 (반대 색상)
          ↓
4. 애플리케이션 구동 대기 (30초)
          ↓
5. 헬스체크 (최대 30초, 3초 간격)
          ↓
6. 성공 시 Nginx 설정 전환 & reload
          ↓
7. 이전 컨테이너 종료
```

### 롤백 전략

- 헬스체크 실패 시 자동 롤백
- 새 컨테이너 종료 후 기존 컨테이너 유지
- Nginx 설정 문법 오류 시 배포 중단

## 인프라 구성

| 서비스 | 사양 / 이미지 |
|--------|--------------|
| EC2 | t3.large, Amazon Linux 2023, 50GB gp3 |
| Nginx | nginx:alpine |
| MySQL | mysql:8.0 |
| Redis | redis:7-alpine |
| Elasticsearch | elasticsearch:8.11.0 + nori |
| 리전 | ap-northeast-2 (서울) |

## 환경변수

### GitHub Secrets

| Name | 설명 |
|------|------|
| `DOCKER_USERNAME` | Docker Hub 아이디 |
| `DOCKER_PASSWORD` | Docker Hub Access Token |
| `EC2_HOST` | EC2 Elastic IP |
| `EC2_KEY` | EC2 pem 키 내용 |

### 애플리케이션 환경변수 (.env.prod)

| Name | 설명 |
|------|------|
| `DB_HOST` | MySQL 호스트 (modeunsa-mysql) |
| `DB_PORT` | MySQL 포트 (3306) |
| `DB_NAME` | 데이터베이스 이름 |
| `DB_USERNAME` | DB 사용자명 |
| `DB_PASSWORD` | DB 비밀번호 |
| `REDIS_HOST` | Redis 호스트 (modeunsa-redis) |
| `REDIS_PORT` | Redis 포트 (6379) |
| `REDIS_PASSWORD` | Redis 비밀번호 |
| `JWT_SECRET` | JWT 시크릿 키 (32바이트 이상) |
| `KAKAO_CLIENT_ID` | 카카오 OAuth 클라이언트 ID |
| `KAKAO_CLIENT_SECRET` | 카카오 OAuth 시크릿 |
| `KAKAO_REDIRECT_URI` | 카카오 리다이렉트 URI |
| `NAVER_CLIENT_ID` | 네이버 OAuth 클라이언트 ID |
| `NAVER_CLIENT_SECRET` | 네이버 OAuth 시크릿 |
| `NAVER_REDIRECT_URI` | 네이버 리다이렉트 URI |
| `AWS_ACCESS_KEY` | AWS 액세스 키 |
| `AWS_SECRET_KEY` | AWS 시크릿 키 |
| `ENCRYPTION_MASTER_KEY` | 암호화 마스터 키 |
| `TOSS_PAYMENTS_SECRET_KEY` | 토스 페이먼츠 시크릿 |
| `ES_HOST` | Elasticsearch 호스트 |
| `ES_PORT` | Elasticsearch 포트 (9200) |

## 최초 EC2 설정

```bash
# 1. EC2 접속
ssh -i modeunsa_pem.pem ec2-user@<EC2_IP>

# 2. 디렉토리 생성
mkdir -p /home/ec2-user/app/config/env

# 3. .env.prod 파일 생성
nano /home/ec2-user/app/config/env/.env.prod

# 4. Systemd 서비스 등록 (부팅 시 자동 시작)
sudo cp /home/ec2-user/app/infra/systemd/modeunsa.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable modeunsa.service

# 5. GitHub에서 main 브랜치 push → 자동 배포!
```

## 수동 배포
```bash
# EC2 접속
ssh -i modeunsa_pem.pem ec2-user@<EC2_IP>

# 앱 디렉토리 이동
cd /home/ec2-user/app

# 수동 배포
./infra/scripts/deploy.sh <DOCKER_USERNAME>/modeunsa:latest
```

## 컨테이너 관리
```bash
# 상태 확인
docker ps

# 로그 확인
docker logs app-blue
docker logs app-green
docker logs nginx
docker logs modeunsa-mysql
docker logs modeunsa-redis
docker logs modeunsa-elasticsearch

# 컨테이너 재시작
docker restart nginx

# 전체 인프라 재시작
cd /home/ec2-user/app
docker-compose -f infra/docker/docker-compose.yml --env-file config/env/.env.prod up -d
```

## 포트 구성

| 포트 | 서비스 | 외부 접근 |
|------|--------|----------|
| 80 | Nginx | ✅ |
| 8081 | app-blue | ❌ (내부) |
| 8082 | app-green | ❌ (내부) |
| 3306 | MySQL | ❌ (내부) |
| 6379 | Redis | ❌ (내부) |
| 9200 | Elasticsearch | ❌ (내부) |

## 트러블슈팅

### 배포 실패 시
```bash
# 최근 배포 로그 확인
docker logs --tail 100 app-blue
docker logs --tail 100 app-green

# Nginx 설정 검증
docker exec nginx nginx -t

# 헬스체크 수동 테스트
curl http://localhost:8081/actuator/health/readiness
curl http://localhost:8082/actuator/health/readiness
```

### 컨테이너가 시작되지 않을 때
```bash
# Docker 서비스 상태 확인
sudo systemctl status docker

# 네트워크 확인
docker network ls
docker network inspect modeunsa-net

# 볼륨 확인
docker volume ls
```

### EC2 재부팅 후 서비스 확인
```bash
# Systemd 서비스 상태
sudo systemctl status modeunsa.service

# 수동으로 서비스 시작
sudo systemctl start modeunsa.service
```