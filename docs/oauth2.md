# OAuth2 소셜 로그인 및 계정 연동 시스템 가이드

이 시스템은 Kakao, Naver 등 다양한 소셜 로그인 공급자를 지원하며, 신규 회원 가입, 기존 회원의 소셜 계정 연동, 그리고 JWT 기반의 인증 시스템과의 통합을 담당합니다.

## 1. 시스템 아키텍처 및 전략

본 시스템은 확장성과 유지보수성을 위해 다음과 같은 패턴을 적용하고 있습니다.

* **전략 패턴 (Strategy Pattern)**: `OAuthClient` 인터페이스를 통해 공급자별(Kakao, Naver) 상세 로직을 캡슐화하고, `OAuthClientFactory`를 통해 동적으로 적절한 클라이언트를 선택합니다.
* **유스케이스 분리 (UseCase Pattern)**: 로그인, 가입, 연동 등 각 기능을 독립적인 UseCase로 분리하여 비즈니스 로직의 응집도를 높였습니다.
* **파사드 패턴 (Facade Pattern)**: 여러 UseCase가 조합된 복합적인 로직을 `AuthFacade`에서 통합 관리하여 컨트롤러와의 의존성을 단순화합니다.

---

## 2. 주요 기능 및 흐름

### 2.1 소셜 로그인 및 회원가입 (`OAuthLoginUseCase`)

1. **인가 코드 수신**: 프론트엔드로부터 `code`와 `state`를 전달받습니다.
2. **State 검증**: CSRF 방지를 위해 Redis에 저장된 `state` 값과 비교 검증합니다.
3. **토큰 교환 및 정보 조회**: 소셜 공급자(Kakao/Naver)로부터 `AccessToken`을 받고, 이를 통해 사용자 프로필 정보를 가져옵니다.
4. **계정 식별 및 가입**: `providerId`를 기준으로 기존 계정을 조회하며, 없을 경우 신규 회원으로 등록합니다. 신규 가입 시에는 `MemberSignupEvent`를 발행하여 타 도메인과의 결합도를 낮춥니다.
5. **JWT 발급**: 최종적으로 `memberId` 기반의 Access/Refresh Token을 생성하여 반환합니다.

### 2.2 소셜 계정 연동 (`OAuthAccountLinkUseCase`)

이미 서비스에 가입된 사용자가 마이페이지 등에서 다른 소셜 계정을 연결하는 기능입니다.

* 현재 로그인된 사용자의 `memberId`를 기반으로 새로운 `OAuthAccount` 엔티티를 생성하여 기존 `Member`와 연결합니다.
* 중복 연동 방지 로직이 포함되어 있어, 이미 다른 회원이 사용 중인 소셜 계정은 연동할 수 없습니다.

---

## 3. 데이터 도메인 모델

### 3.1 엔티티 구조

* **Member**: 핵심 사용자 정보를 담고 있습니다.
* **OAuthAccount**: 소셜 공급자 정보(`provider`)와 고유 식별값(`providerId`)을 저장하며, `Member`와 N:1 양방향 연관관계를 맺습니다. 한 사용자는 여러 소셜 계정을 가질 수 있습니다.

### 3.2 토큰 관리 (Redis 연동)

* **AuthRefreshToken**: 사용자의 세션 유지를 위해 Redis에 `memberId`를 키로 저장하며, TTL을 통해 만료를 자동 관리합니다.
* **AuthAccessTokenBlacklist**: 로그아웃 시 사용된 Access Token을 남은 유효 시간만큼 Redis에 등록하여 탈취된 토큰의 재사용을 방지합니다.

---

## 4. 예외 및 보안 처리

* **Redirect URI 검증**: `SecurityProperties`에 등록된 허용된 도메인만 `redirect_uri`로 사용할 수 있도록 엄격하게 검증합니다.
* **동시성 처리**: 동시 가입 요청 시 발생할 수 있는 데이터 중복 문제를 `DataIntegrityViolationException` 캐치 및 재조회 로직을 통해 방어합니다.

---

## 5. 설정 및 확장 방법

새로운 소셜 로그인 공급자(예: Google, Apple)를 추가하려면 다음 단계를 따릅니다.

1. `OAuthProvider` Enum에 항목 추가.
2. `OAuthClient` 인터페이스를 구현하는 새로운 클라이언트 클래스 작성.
3. `application.yml`의 `spring.security.oauth2.client.registration` 하위에 클라이언트 정보 등록.