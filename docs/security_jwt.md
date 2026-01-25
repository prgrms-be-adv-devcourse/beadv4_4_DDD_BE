# Spring Security + JWT 인증 시스템 구현 정리

이 시스템은 **Stateless** 환경에서 **JWT(JSON Web Token)**를 사용하여 사용자의 신원을 확인하고 권한을 관리합니다.

## 1. 시스템 아키텍처 및 흐름

1. **요청 수신**: 클라이언트가 `Authorization` 헤더에 `Bearer <JWT>`를 담아 API를 요청합니다.
2. **필터 검증 (`JwtAuthenticationFilter`)**: 모든 요청은 Spring Security 필터 체인을 통과하며, JWT의 유효성을 검사합니다.
3. **토큰 해석 (`JwtTokenProvider`)**: Secret Key를 사용하여 토큰을 파싱하고, `memberId`와 `role`을 추출합니다.
4. **인증 정보 저장**: 유효한 토큰인 경우 `SecurityContextHolder`에 인증 객체(`Authentication`)를 저장하여 후속 로직에서 사용자 정보를 사용할 수 있게 합니다.
5. **접근 제어 (`SecurityConfig`)**: 설정된 권한 규칙에 따라 요청 허용 여부를 결정합니다.

---

## 2. 주요 구성 요소

### 🛠 JwtTokenProvider (토큰 생성 및 검증)

JWT의 생명주기를 관리하는 핵심 컴포넌트입니다.

* **생성**: `jjwt` 라이브러리를 사용하여 Access Token과 Refresh Token을 생성합니다.
* **검증**: 토큰의 만료 여부, 서명 일치 여부, 토큰 타입(Access/Refresh)을 확인합니다.
* **정보 추출**: Claims에서 사용자의 식별자(Subject)와 권한(Role)을 꺼내옵니다.

### 🛡 JwtAuthenticationFilter (인증 필터)

`OncePerRequestFilter`를 상속받아 요청당 한 번씩 실행됩니다.

* **Blacklist 확인**: 로그아웃된 토큰인지 Redis 등을 통해 조회합니다.
* **Context 설정**: 인증에 성공하면 `CustomUserDetails`를 담은 `UsernamePasswordAuthenticationToken`을 생성하여 Context에 등록합니다.
* **예외 전달**: 검증 중 발생한 예외를 `HttpServletRequest`에 속성으로 저장하여 EntryPoint에서 처리할 수 있도록 합니다.

### ⚙ SecurityConfig (보안 설정)

애플리케이션의 전반적인 보안 정책을 정의합니다.

* **무상태성(Stateless)**: 세션을 사용하지 않도록 `SessionCreationPolicy.STATELESS`로 설정합니다.
* **계층형 권한(Role Hierarchy)**: `SYSTEM, HOLDER > ADMIN > SELLER > MEMBER` 순으로 상위 권한이 하위 권한을 포함하도록 설정되어 있습니다.
* **CORS 설정**: 허용된 Origin(`modeunsa.store` 등)과 메서드를 정의하여 프론트엔드와의 통신을 지원합니다.

---

## 3. 예외 처리 메커니즘

인증 및 인가 과정에서 발생하는 에러는 별도의 핸들러를 통해 일관된 JSON 응답으로 반환됩니다.

| 구분 | 클래스 | HTTP 상태 코드 | 발생 상황 |
| --- | --- | --- | --- |
| **Authentication** | `JwtAuthenticationEntryPoint` | 401 Unauthorized | 토큰이 없거나, 만료되었거나, 잘못된 경우 |
| **Authorization** | `JwtAccessDeniedHandler` | 403 Forbidden | 인증은 되었으나 해당 리소스에 접근 권한이 없는 경우 |

---

## 4. 권한 설계 (Role Hierarchy)

본 프로젝트는 단순한 권한 체크를 넘어 계층 구조를 적용하고 있습니다. 이를 통해 관리자(`ADMIN`)는 판매자(`SELLER`)나 일반 회원(`MEMBER`)의 API에 별도 설정 없이 접근할 수 있습니다.

```java
public RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.withDefaultRolePrefix()
        .role("SYSTEM").implies("ADMIN")
        .role("HOLDER").implies("ADMIN")
        .role("ADMIN").implies("SELLER")
        .role("SELLER").implies("MEMBER")
        .build();
}
```

---

## 5. 보안 설정 요약 (SecurityConfig.java)

* **CSRF/FormLogin 비활성화**: JWT 기반이므로 불필요한 보안 기능을 끕니다.
* **PermitAll**: `SecurityProperties`를 통해 YAML 설정에서 공개 URL을 유연하게 관리합니다.
* **Method Security**: `@PreAuthorize` 등의 어노테이션에서도 계층형 권한이 작동하도록 `MethodSecurityExpressionHandler`를 빈으로 등록했습니다.