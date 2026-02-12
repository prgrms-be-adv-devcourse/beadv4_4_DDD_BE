# 멀티모듈 + Monorepo 전환 가이드라인

### 핵심 원칙 요약
* common은 구현을 가지지 않는다.
* 호출 구현은 호출하는 쪽에 둔다.
* 실행 모듈만 SpringBoot Application을 가진다.
* 의존성 방향은 단방향으로 유지한다.

### 빠른 실행
1. 모듈 생성
2. build.gradle 수정
    1. build.gradle 파일 제외 기본적으로 생성되는 파일은 삭제해도됨. ex. gradle 디렉토리, .gitignore 등
    2. application-{env}.yml 파일 추가
        1. application.yml → port 수정
        2. application-dev.yml
            1. `cors:allowed-origins` 추가
            2. `custom:swagger:serverUrl` 수정
3. root/settings.gradle 에 도메인 추가 `include 'product'`
4. {module}Application 어노테이션 추가
    1. `@EnableJpaAuditing(auditorAwareRef = "userAuditorAware")`
5. securityconfig 추가 (임시)
6. com/modeunsa/boundedcontext/{module} path 맞춰서 패키지 이동
7. common 모듈로 global, shared 관련 내용 이동
8. api 모듈에 남아있는 기존 모듈 패키지 삭제
9. 모듈 관련 test 이동
10. {module}Application 실행 후 swagger 정상 진입 & port 확인

## 1. 목적

기존 모놀리식 구조에서 점진적으로 서비스 분리를 진행하기 위함
- Gradle 멀티모듈 구조 도입
- 도메인 단위 모듈 분리
- 내부 HTTP 통신 기반 구조로 전환
- 향후 MSA 확장 가능하도록 설계

---

## 2. 최종 아키텍처 구조 
```
modeunsa (Monorepo)
 ├─ gradlew
 ├─ settings.gradle
 ├─ api        (8080)  → BFF 역할 or Gateway
 ├─ product    (8081)  → 상품 도메인 서비스
 ├─ order      (8082)  → 주문 도메인 서비스
 ├─ payment    (8083)  → 결제 도메인 서비스
 ├─ settlement (8084)  → 정산 도메인 서비스
 ├─ member     (8086)  → 회원 도메인 서비스
 ├─ auth       (8087)  → 인증 도메인 서비스
 ├─ file       (8088)  → 파일 도메인 서비스
 ├─ content    (8089)  → 컨텐츠 도메인 서비스
 └─ common     → 공통 라이브러리
```

---

## 3. 모듈 책임 정의

### api 모듈 (8080)
- 외부 클라이언트 진입점
- 인증/인가 처리
- 화면 단위 집계 로직
- 내부 서비스 호출 (HTTP)

### 금지 사항
- 도메인 로직 직접 처리
- 타 모듈 DB 직접 접근

---

### domain 모듈 (8081~8089)

- 개별 도메인 책임
- 내부 API 제공 (`/internal/**`)
- 독립 실행 가능

### 금지 사항
- 도메인 로직 직접 처리
- 타 모듈 DB 직접 접근

---

### common 모듈

- 공통 DTO
- 공통 Exception
- 공통 인터페이스
- 유틸 클래스

### 금지 사항
- SpringBootApplication
- SecurityConfig
- RestClient 구현체
- 특정 도메인 의존

---

## 4. 의존성 방향 원칙

### 반드시 지켜야 할 구조
```
api → product
api → common
product → common
```
### 금지 구조
```
common → product
product → api
api ↔ product 직접 코드 참조
```

---

## 5. ApiClient 위치 규칙

### 잘못된 위치
- common/ProductApiClient


### 올바른 위치
- api/ProductApiClient

> 원칙: “클라이언트 구현은 호출하는 쪽에 둔다.”

---

## 6. 내부 HTTP 호출 규칙

### Base URL 규칙

```yaml
custom:
  global:
    productInternalBackUrl: http://localhost:8081
```

### Client 구현 원칙
```java
.baseUrl(internalBackUrl)
.uri("/api/v1/products/internal/validate-order")
```

## 7. Swagger 관리 전략
### 현재 단계
* 각 서비스 독립 Swagger 유지
* 필요 시 api에서 aggregation
```yaml
# application-dev.yml
custom:
  swagger:
    serverUrl: http://localhost:{port}
    description: "{module_name} Local Server"
```

## 8. Security 원칙
### 기본 모듈
* 가능하면 Security 제거
* 내부 서비스는 BFF를 통해 보호

### api 모듈
* JWT 인증 처리
* 외부 접근 제어

## 9. JPA Auditing 규칙
* @EnableJpaAuditing은 실행 모듈에만 둔다.
* common에 두지 않는다.
* AuditorAware는 Security 의존 모듈에 둔다.

## 10. Gradle 구조 규칙
### 루트에만 존재
* gradlew
* gradle/wrapper
* settings.gradle


각 모듈에는 **build.gradle**만 존재한다.

## 11. Git 관리 전략
* Monorepo 유지
* 루트에만 .gitignore
* 모듈별 .gitignore 불필요