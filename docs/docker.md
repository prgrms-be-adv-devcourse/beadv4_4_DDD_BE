# 🐳 Docker & Docker Compose 가이드

이 문서는 '모든사(modeunsa)' 프로젝트의 컨테이너 환경 구축 및 배포를 위한 가이드를 제공합니다. Docker를 통해 인프라 의존성을 격리하고, 어떤 환경에서도 동일하게 동작하는 실행 환경을 보장합니다.

## 1. 애플리케이션 컨테이너화 (Dockerfile)

루트 경로의 `Dockerfile`은 Spring Boot 애플리케이션을 효율적이고 보안성이 높은 가벼운 이미지로 빌드합니다.

### 🛡️ 주요 보안 및 최적화 설정

* **Base Image**: 가볍고 보안에 강한 `eclipse-temurin:21-jre-alpine`을 사용합니다.
* **Non-root User**: 보안 강화를 위해 `appuser`라는 비관리자 사용자를 생성하고, 해당 권한으로 애플리케이션을 실행합니다.
* **실행 프로파일**: 컨테이너 실행 시 `-Dspring.profiles.active=prod` 옵션을 통해 운영 환경 설정을 강제합니다.

---

## 2. 다중 컨테이너 오케스트레이션 (docker-compose.yml)

`docker-compose`를 사용하여 웹 서버, 애플리케이션, 데이터베이스, 캐시, 검색 엔진을 하나의 네트워크(`modeunsa-net`)로 묶어 관리합니다.

### 🏗️ 서비스 구성 요소

| 서비스 | 이미지 | 역할 | 비고 |
| --- | --- | --- | --- |
| **nginx** | `nginx:alpine` | 리버스 프록시 및 SSL 처리 | 80, 443 포트 개방 |
| **app** | `sohee52/modeunsa` | Spring Boot 애플리케이션 | 환경 변수를 통한 설정 주입 |
| **mysql** | `mysql:8.0` | 메인 관계형 데이터베이스 | 데이터 영속성을 위한 볼륨 마운트 |
| **redis** | `redis:7-alpine` | 세션 및 블랙리스트 캐시 | 비밀번호 기반 보안 적용 |
| **elasticsearch** | 커스텀 빌드 | 검색 엔진 (Nori 분석기 포함) | 한글 형태소 분석 지원 |

---

## 3. 검색 엔진 커스텀 빌드 (Elasticsearch)

한국어 검색 품질 향상을 위해 공식 Elasticsearch 이미지에 **Nori(노리) 한글 형태소 분석기**를 설치하여 사용합니다.

### 📂 프로젝트 구조 및 설정

* **구조**: `./docker/elasticsearch/Dockerfile` 경로에 위치합니다.
* **커스텀 로직**: `elasticsearch:9.2.2` 버전을 기반으로 `analysis-nori` 플러그인을 설치합니다.

---

## 4. 환경 변수 및 볼륨 관리

### 🔐 보안 데이터 관리

`docker-compose.yml` 내부의 민감한 정보(비밀번호, API 키 등)는 `${VARIABLE_NAME}` 형식을 통해 외부 환경 변수나 `.env` 파일에서 주입받아 보안을 유지합니다.

### 💾 데이터 영속성 (Persistence)

컨테이너가 삭제되어도 데이터가 소실되지 않도록 다음 볼륨들을 설정했습니다:

* `mysql_data`: MySQL 데이터 저장
* `redis_data`: Redis 데이터 저장
* `es_data`: Elasticsearch 색인 데이터 저장

---

## 5. 실행 및 관리 명령어

### 🚀 인프라 가동

```bash
# 백그라운드에서 모든 서비스 실행
docker-compose up -d

# 특정 서비스(예: app)만 재빌드 및 실행
docker-compose up -d --build app

```

### 📊 모니터링

```bash
# 실행 중인 컨테이너 상태 확인
docker-compose ps

# 실시간 로그 확인
docker-compose logs -f [service_name]

```