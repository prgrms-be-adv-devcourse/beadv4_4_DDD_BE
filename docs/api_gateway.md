## API Gateway

이번 아키텍처 개편을 통해 API Gateway(api 모듈)는 시스템의 최전방 수문장 역할을 담당하며, 인증(Authentication)을 중앙화하고 인가(Authorization)를 각 마이크로서비스로 분산시키는 역할을 수행합니다.

### 1. 포트 연결 및 라우팅 흐름 (Port & Routing)

모든 클라이언트(프론트엔드)의 요청은 개별 모듈 포트(예: 8086, 8088)가 아닌 **API Gateway 포트(8080)** 로 집중됩니다.

- 요청 파이프라인: Client(프론트엔드) ➡️ API Gateway(8080) ➡️ Gateway Auth Filter ➡️ 각 다운스트림 모듈(8086, 8088 등)
- 라우팅 원리: Gateway는 요청된 URL(예: /api/v1/files/**)을 확인하고, 유레카(Eureka) 또는 설정된 라우팅 룰에 따라 해당 도메인을 담당하는 마이크로서비스로 요청을 안전하게 토스합니다.

### 2. 전체 인증/인가 파이프라인

* **[Client]** ➡️ (Bearer Token) ➡️ **[API Gateway]** * **[API Gateway]** ➡️ (WebClient 비동기 검증 요청) ➡️ **[Member 모듈 (/me)]**
* **[API Gateway]** ⬅️ (검증 결과 및 User Info 반환) ⬅️ **[Member 모듈]**
* **[API Gateway]** ➡️ (`X-User-Id`, `X-User-Role` 헤더 주입) ➡️ **[각 다운스트림 모듈 (File, Product 등)]**
* **[다운스트림 모듈]** ➡️ (자체 `SecurityConfig`로 최종 권한 확인 후 로직 수행)

### 3. Gateway의 핵심 역할

* **인증의 중앙화:** 더 이상 각 다운스트림 모듈이 JWT Secret Key를 들고 직접 토큰을 뜯어보지 않습니다. Gateway가 Auth 모듈에 검증을 위임하고 결과만 헤더로 안전하게 전달합니다.
* **내부망 보안 (Internal API Key):** 외부에서 내부망 전용 API(`/api/*/*/internal/**`)로 직접 접근하는 것을 차단하고, MSA 모듈 간 통신 시 발급된 `X-INTERNAL-API-KEY`를 통해 안전하게 통신합니다.
* **CORS 중앙 통제:** 개별 모듈에 흩어져 있던 CORS 설정을 Gateway로 일원화하여 프리플라이트(Preflight) 요청과 다중 헤더 충돌 이슈를 원천 차단했습니다.

### 4. 주요 추가 및 변경 파일 설명

`api` 모듈(Gateway) 내부에 새롭게 추가된 핵심 파일들의 역할입니다.

**`AuthenticationGlobalFilter.java`**
* **역할:** Gateway를 통과하는 모든 요청을 가로채는 핵심 글로벌 인증 필터입니다.
* **기능 1:** `application.yml`의 `permit-urls`에 등록된 공개 주소는 인증 검사 없이 통과시킵니다.
* **기능 2:** 내부망 전용 API 호출 시 `X-INTERNAL-API-KEY` 헤더의 유효성을 검증하여 시스템 계정(`SYSTEM`) 권한을 부여합니다.
* **기능 3:** 일반 API 호출 시 `AuthServiceClient`를 통해 토큰을 검증하고, 다운스트림으로 보낼 요청 헤더를 재조립합니다.


**`AuthServiceClient`**
* **역할:** Gateway(WebFlux) 환경에 맞춰 비동기 논블로킹 방식으로 Member 모듈과 통신하는 클라이언트입니다.
* **기능:** WebClient를 사용하여 Member 모듈의 `/me` API를 호출하고, 그 응답인 `AuthStatusResponse`를 가져옵니다.


**`SecurityConfig.java` (Gateway 전용)**
* **역할:** Spring Cloud Gateway(WebFlux)를 위한 시큐리티 설정입니다.
* **특징:** 실질적인 토큰 검증은 커스텀 필터(`AuthenticationGlobalFilter`)에서 수행하므로, 여기서는 기본적인 Security Web Filter Chain 규칙을 모두 허용(`permitAll()`) 처리하여 충돌을 방지합니다.

**전역 CORS 설정 적용 (WebMvcConfig 중복 제거)**
* 개별 모듈에 흩어져 있던 CORS 설정(addCorsMappings)을 제거하고, Gateway 레벨에서 전역적으로 CORS 헤더를 제어하도록 구성하여 브라우저의 다중 헤더 차단 에러를 원천 차단했습니다.
