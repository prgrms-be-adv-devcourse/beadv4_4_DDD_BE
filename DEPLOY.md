# 🚀 modeunsa 배포 가이드 (Deployment Guide)

이 문서는 **modeunsa** 프로젝트의 지속적 통합(CI) 및 지속적 배포(CD) 구조와 서버 운영 방법을 설명합니다.

## 🏗 CI/CD 아키텍처

우리 프로젝트는 **GitHub Actions**를 사용하여 개발부터 배포까지의 전 과정을 자동화합니다.

1. **CI (Continuous Integration)**: 모든 Pull Request에 대해 코드 품질과 안정성을 검증합니다.
2. **CD (Continuous Deployment)**: `main` 브랜치에 코드가 반영되면 AWS EC2 서버에 자동으로 배포합니다.

---

## 🔐 사전 설정 (Prerequisites)

배포를 시작하기 전, GitHub 리포지토리의 **Settings > Secrets and variables > Actions**에 아래 항목들이 등록되어 있어야 합니다.

| 구분 | Key | 설명 |
| --- | --- | --- |
| **인프라** | `EC2_HOST`, `EC2_KEY` | AWS EC2 접속 정보 (SSH용) |
| **Docker** | `DOCKER_USERNAME`, `DOCKER_PASSWORD` | Docker Hub 계정 정보 |
| **보안/인증** | `JWT_SECRET`, `ENCRYPTION_MASTER_KEY` | 토큰 및 데이터 암호화 키 |
| **DB/Redis** | `DB_PASSWORD`, `REDIS_PASSWORD` | 데이터베이스 및 캐시 접속 비밀번호 |
| **OAuth** | `KAKAO_...`, `NAVER_...` | 소셜 로그인 API 키 |

---

## 🔄 파이프라인 상세

### 1. CI: Pull Request 검증 (`pr-check.yml`)

* **Trigger**: `main`, `develop` 브랜치로의 PR 생성 및 업데이트
* **주요 작업**:
* **Code Style**: Spotless 및 Checkstyle을 통해 코드 컨벤션을 준수하는지 확인합니다.
* **Test**: Java 21 환경에서 Redis 컨테이너를 구동하여 통합 테스트를 수행합니다.
* **Build**: 테스트 제외 빌드를 통해 JAR 파일 생성 가능 여부를 체크합니다.



### 2. CD: 자동 배포 (`deploy.yml`)

* **Trigger**: `main` 브랜치에 코드 Push 또는 Merge
* **주요 작업**:
* **Multi-Tagging**: 빌드된 이미지는 `YYYYMMDD-번호` 태그와 `latest` 태그를 동시에 부여받습니다.
* **File Sync**: `docker-compose.yml`, `nginx.conf`, 그리고 **`docker/` 폴더(Elasticsearch용)**를 EC2로 전송합니다.
* **Local Build**: EC2 서버에서 직접 `analysis-nori` 플러그인이 포함된 Elasticsearch 이미지를 빌드합니다.
* **Cleanup**: 배포 완료 후 사용하지 않는 이전 Docker 이미지들을 삭제하여 용량을 확보합니다.



---

## 🌐 서버 구성 (EC2 내부)

### 1. Nginx 설정 (`nginx.conf`)

* **HTTPS**: Let's Encrypt SSL 인증서를 사용하여 443 포트로 보안 통신을 제공합니다.
* **Reverse Proxy**: 모든 외부 요청을 내부망의 `app:8080`으로 전달합니다.

### 2. Docker Compose 서비스

* **app**: Spring Boot 백엔드 애플리케이션 (Java 21)
* **mysql**: 데이터 저장소 (8.0 버전)
* **redis**: 세션 및 캐시 관리 (7-alpine 버전)
* **elasticsearch**: 한글 검색 엔진 (nori 플러그인 탑재)

---

## 🛠 유지보수 명령어 (Useful Commands)

EC2 서버(`~/app` 경로)에서 자주 사용하는 명령어입니다.

```bash
# 전체 서비스 상태 확인
docker-compose ps

# 특정 서비스 로그 확인 (예: Elasticsearch)
docker-compose logs -f elasticsearch

# 환경변수(.env) 수정 후 강제 재시작 및 빌드
docker-compose --env-file ./.env up -d --build --remove-orphans

# 사용하지 않는 Docker 리소스 일괄 정리
docker system prune -a

```

---

## ⚠️ 주의 사항

* **환경 변수**: `.env` 파일은 보안상 GitHub에 올리지 않으며, 서버에 직접 생성하거나 관리해야 합니다.
* **인증서 갱신**: SSL 인증서 만료 전 `certbot`을 통한 갱신이 필요할 수 있습니다 (현재 Nginx 볼륨 마운트 방식).