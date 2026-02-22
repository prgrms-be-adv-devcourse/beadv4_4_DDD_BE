## API Gateway

이번 아키텍처 개편을 통해 API Gateway(api 모듈)는 시스템의 최전방 수문장 역할을 담당하며, 인증(Authentication)을 중앙화하고 인가(Authorization)를 각 마이크로서비스로 분산시키는 역할을 수행합니다.

### 1. 포트 연결 및 라우팅 흐름 (Port & Routing)

모든 클라이언트(프론트엔드)의 요청은 개별 모듈 포트(예: 8086, 8088)가 아닌 **API Gateway 포트(8080)** 로 집중됩니다.

* **요청 파이프라인:** Client(프론트엔드) ➡️ API Gateway(8080) ➡️ Gateway Auth Filter ➡️ 각 다운스트림 모듈(8086, 8088 등)
* **라우팅 원리:** Gateway는 요청된 URL(예: `/api/v1/files/**`)을 확인하고, 유레카(Eureka) 또는 설정된 라우팅 룰에 따라 해당 도메인을 담당하는 마이크로서비스로 요청을 안전하게 토스합니다.

### 2. 전체 인증/인가 파이프라인

* **[Client]** ➡️ (Bearer Token) ➡️ **[API Gateway]** ➡️ (WebClient 비동기 검증 요청) ➡️ **[Member 모듈 (/me)]**
* **[Member 모듈]** ➡️ (검증 결과 및 User Info 반환) ➡️ **[API Gateway]**
* **[API Gateway]** ➡️ (`X-User-Id`, `X-User-Role` 헤더 주입) ➡️ **[각 다운스트림 모듈 (File, Product 등)]**
* **[다운스트림 모듈]** ➡️ (자체 `SecurityConfig`로 최종 권한 확인 후 로직 수행)

### 3. Gateway의 핵심 역할

* **인증의 중앙화:** 더 이상 각 다운스트림 모듈이 JWT Secret Key를 들고 직접 토큰을 뜯어보지 않습니다. Gateway가 Auth 모듈에 검증을 위임하고 결과만 헤더로 안전하게 전달합니다.
* **내부망 보안 (Internal API Key):** 외부에서 내부망 전용 API(`/api/*/*/internal/**`)로 직접 접근하는 것을 차단하고, MSA 모듈 간 통신 시 발급된 `X-INTERNAL-API-KEY`를 통해 안전하게 통신합니다.
* **CORS 중앙 통제:** 개별 모듈에 흩어져 있던 CORS 설정을 Gateway로 일원화하여 프리플라이트(Preflight) 요청과 다중 헤더 충돌 이슈를 원천 차단했습니다.

### `api-gateway` 모듈에 추가된 파일들의 역할

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

### **`common` 모듈** 내부에 추가된 파일들의 역할

**`GatewayHeaderFilter.java`**
* **역할:** API Gateway에서 넘겨준 HTTP 헤더(`X-User-Id`, `X-User-Role` 등)를 읽어 다운스트림 모듈의 Spring Security `SecurityContext`를 구성하는 핵심 필터입니다. 다운스트림 모듈의 기존 JWT 필터를 대체합니다.


**`CustomAuthenticationEntryPoint.java`**
* **역할:** 인증 정보가 없는 사용자(Gateway 헤더 누락 등)가 보호된 자원에 접근할 때 401 Unauthorized 예외를 표준화된 포맷으로 반환하는 처리기입니다.


**`CustomAccessDeniedHandler.java`**
* **역할:** 인증은 되었으나 해당 API를 호출할 권한(Role)이 부족한 사용자가 접근할 때 403 Forbidden 예외를 반환하는 처리기입니다.


---

### 5. 공지 사항 및 아키텍처 변화

API Gateway 인증 관련 PR이 머지되었습니다. 아래 내용을 참고하여 각 다운스트림 모듈에 적용해 주세요!

- **분산 인가 방식 채택:** Gateway 인가 전략 중 **'분산 인가 방식'** 으로 진행합니다. (상세 내용은 관련 [이슈의 댓글](https://github.com/prgrms-be-adv-devcourse/beadv4_4_DDD_BE/issues/618#issuecomment-3939073255)을 확인해 주세요.)
- **컨트롤러 아키텍처 변화:** 이제 각 다운스트림 모듈의 컨트롤러에서 토큰을 직접 확인하지 않습니다. `@AuthenticationPrincipal CustomUserDetails`는 `GatewayHeaderFilter`가 헤더를 기반으로 자동 생성해 줍니다.
- **라우팅 및 권한 설정 구분 가이드:**
    - **`SecurityConfig`:** 특정 Role이나 HTTP Method 등 **인가(Authorization)** 와 관련된 코드를 추가합니다. (이 내용은 API Gateway에 추가하지 않아도 됩니다.)
    - **`application.yml` (`permit-urls`):** **인증 통과(허용)** 가 필요한 URL을 추가합니다. 이곳에 추가되는 내용은 반드시 API Gateway의 `yml`에도 동일하게 추가되어야 정상적으로 인증을 통과합니다.

### 6. 다운스트림 모듈 적용 방법

변경된 인증 방식을 각 모듈에 적용하는 방법입니다. (시범적으로 **File 모듈**에 적용되어 있으니, 작업 시 해당 모듈의 파일들을 참고해 주세요.)

**`build.gradle` 수정**

- **삭제할 것:** `member` 모듈, JWT 및 OAuth 관련 의존성을 모두 제거합니다.
- **남길 것:** `spring-boot-starter-security` (권한 체크를 위해 필수로 남겨둡니다.)

**`application-dev.yml` 수정 (라우팅 및 스웨거 설정)**

- **`permit-urls`:** Swagger, Actuator 등 공통 도구만 남기고, 타 모듈의 주소는 모두 지웁니다.
- **Swagger `serverUrl`:** 반드시 API Gateway 주소인 `http://localhost:8080` 으로 맞춰줍니다. (`custom.swagger.serverUrl: "http://localhost:8080"`)
- **설정 다이어트:** `oauth2`, `jwt`, `cors` 등 더 이상 개별 모듈에서 처리하지 않는 설정은 제거해 주세요.
- **참고:** `k3s-dev`, `k3s-prod` 환경 파일에도 동일한 원칙으로 적용해 줍니다.

**`SecurityConfig.java` 수정 (권한 통제 : 인가)**

- **`SecurityProperties` 추가:** 클래스 레벨에 관련 프로퍼티를 추가합니다.
- **공통 필터 주입:** 기존 `JwtAuthenticationFilter` 대신, `common` 모듈에서 만든 `GatewayHeaderFilter`, `CustomAuthenticationEntryPoint`, `CustomAccessDeniedHandler`를 주입받아 필터 체인에 등록합니다.
- **내 도메인만 관리:** `.requestMatchers()`에는 **오직 자신의 모듈과 관련된 주소**만 남기고 나머지는 깔끔하게 지웁니다.
- **CORS 설정 제거:** API Gateway에서 전역으로 CORS 처리를 담당하므로, 다운스트림 모듈 내부의 CORS 관련 코드는 모두 제거합니다.
- **`roleHierarchy()` 제거:** 해당 계층 설정 메서드는 `common` 모듈에 통합 추가되었으므로, 개별 모듈에서는 작성하지 않아도 자동으로 적용됩니다.

> 기존에 공통으로 작성되던 권한 허용 URL 관련 내용은 **팀 Notion의 '문서' 페이지**에 정리되어 있습니다.