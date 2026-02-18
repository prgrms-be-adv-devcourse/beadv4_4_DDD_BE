## 결제 Saga & 상태머신 설계 가이드

### 1. 목표

- `Payment` 엔티티를 **명시적인 상태머신**으로 사용해 결제 진행/실패/보상 흐름을 일관성 있게 관리한다.
- 외부 PG(Toss Payments) 호출을 포함한 **각 단계별 재시도/최종 실패 처리 규칙**을 정한다.
- 나중에 **Saga 로깅(AOP, traceId, Grafana)** 을 쉽게 붙일 수 있도록 단계/이벤트를 명확히 한다.

---

### 2. Payment 상태머신 정리

#### 2.1 상태 집합 (`PaymentStatus`)

실제 코드: `domain/types/PaymentStatus.java` (description 필드 있음)

```java
public enum PaymentStatus {
  PENDING("결제 대기"),
  IN_PROGRESS("결제 진행"),
  APPROVED("결제 승인"),
  CANCELED("결제 취소"),
  FAILED("결제 실패"),
  FINAL_FAILED("최종 결제 실패"),
  SUCCESS("결제 완료"),
  REFUND_REQUESTED("환불 요청"),
  REFUNDED("환불 완료");
}
```

`Payment.java` 의 전이 허용 상수: `ALLOWED_FOR_PENDING`, `ALLOWED_FOR_IN_PROGRESS`, `ALLOWED_FOR_SUCCESS`, `FINAL_TERMINAL_STATUSES` (최종 상태 = SUCCESS, FINAL_FAILED, CANCELED, REFUNDED).

#### 2.2 전이(Transition) 개념

`Payment` 엔티티의 실제 전이 메서드 (`Payment.java` 기준):

- `changeToInProgress()` : `PENDING/FAILED` → `IN_PROGRESS` (마감일 검증 포함)
- `changeToApprove(TossPaymentsConfirmResponse)` : `IN_PROGRESS` → `APPROVED`
- `changeToSuccess()` : `IN_PROGRESS/APPROVED` → `SUCCESS`
- `updateFailureInfo(PaymentErrorCode, String)` : 실패 정보 저장 후 `errorCode.isFinalFailure()` 에 따라 `changeToFailed(FAILED|FINAL_FAILED, message)` 호출
- `changeToFailed(PaymentStatus, String)` : (어디서든) → `FAILED` or `FINAL_FAILED` + PaymentLog
- `changeToPending(LocalDateTime)` : `PENDING/FAILED` 에서 마감일 갱신 후 PENDING (재시도용)
- `syncToInProgress() / syncToApproved() / syncToCanceled()` : Toss 웹훅 등 외부 이벤트 동기화

이 메서드들이 **상태머신의 “간선”** 역할을 하고,  
`validate*` 메서드들이 **불법 전이를 막는 가드(guard)** 역할을 한다.

> 상태머신의 “두뇌”는 `Payment` 엔티티에 두고,  
> 외부에서는 **반드시 전이 메서드만 사용해서 상태를 바꾸는 것**이 핵심이다.

---

### 3. 상태 관련 validation 정리 전략

현재 `Payment` 안에 있는 validation (`Payment.java` 기준):

- 상태 기반 (private):
  - `validatePaymentStatus(PaymentStatus)` — 현재 상태가 특정 상태인지
  - `validatePaymentStatusContains(Set<PaymentStatus>, PaymentStatus)` — 현재 상태가 허용 집합에 있는지
  - `validateNotTerminalStatus()` — `FINAL_TERMINAL_STATUSES` 에 없어야 함
  - `validateCanChangeToPending()` (상태 + 마감일)
  - `validateCanChangeToInProgress()` (상태 + 마감일)
  - `validateCanChangeToApprove()` — `IN_PROGRESS` 여야 함
- 시간/금액 기반:
  - `validatePaymentDeadline()` (private)
  - `validateTotalAmount(BigDecimal)` (static, create 시)
  - `validateChargeAmount(BigDecimal)` (public, PG 결제 금액 검증)
- 최종 실패 여부는 `PaymentErrorCode.isFinalFailure()` 로 판단하며, `Payment` 엔티티에는 `isTerminalStatus()` / `isRetryable()` 메서드는 없고 `FINAL_TERMINAL_STATUSES` 상수로 가드한다.

#### 3.1 같은 파일 안에서 가독성 높이는 방법

1. **섹션별로 묶어두기**
   - 코드 순서를 크게 4 블록으로 나눈다:
     1. 필드/생성자
     2. **상태 전이 메서드 (public)**  
        - `changeToInProgress`, `changeToApprove`, `changeToSuccess`, `updateFailureInfo` / `changeToFailed`, `changeToPending`, `syncTo*`
     3. **상태 관련 validation (private)**  
        - `validatePaymentStatus`, `validatePaymentStatusContains`, `validateNotTerminalStatus`, `validateCanChangeToPending`, `validateCanChangeToInProgress`, `validateCanChangeToApprove`
     4. **값/마감일/금액 관련 validation**  
        - `validateTotalAmount` (static), `validatePaymentDeadline` (private), `validateChargeAmount` (public)

   - 예시(개념, 실제 `Payment.java` 메서드명 기준):

   ```java
   // 1) public API: 상태 전이 메서드들
   public void changeToInProgress() { ... }
   public void changeToApprove(TossPaymentsConfirmResponse tossRes) { ... }
   public void changeToSuccess() { ... }
   public void updateFailureInfo(PaymentErrorCode errorCode, String failureMessage) { ... }
   public void changeToFailed(PaymentStatus newStatus, String message) { ... }
   public void changeToPending(LocalDateTime paymentDeadlineAt) { ... }
   public void syncToInProgress() { ... }
   public void syncToApproved() { ... }
   public void syncToCanceled() { ... }

   // 2) 상태 관련 validation (private)
   private void validateNotTerminalStatus() { ... }
   private void validatePaymentStatus(PaymentStatus paymentStatus) { ... }
   private void validatePaymentStatusContains(Set<PaymentStatus> allowStatus, PaymentStatus target) { ... }
   private void validateCanChangeToPending() { ... }
   private void validateCanChangeToInProgress() { ... }
   private void validateCanChangeToApprove() { ... }

   // 3) 값/마감일/금액 관련 validation
   private static void validateTotalAmount(BigDecimal totalAmount) { ... }
   private void validatePaymentDeadline() { ... }
   public void validateChargeAmount(BigDecimal chargeAmount) { ... }
   ```

2. **도메인 정책 명시적인 네이밍**
   - `ALLOWED_FOR_IN_PROGRESS`, `ALLOWED_FOR_SUCCESS`, `FINAL_TERMINAL_STATUSES` 처럼  
     **전이 허용 집합**을 상단에 두고, validation 메서드에서 재사용하는 현재 구조는 이미 좋다.
   - 여기에 JavaDoc/주석으로 “왜 이 집합이 필요한지 (도메인 규칙)”만 살짝 붙여주면 이해가 더 쉬워진다.

#### 3.2 별도 클래스로 일부 분리하는 옵션

**분리의 기준**은 “이 로직이 Payment 필드에 직접 접근해야 하는가?”이다.

- 상태/마감일/금액 모두 `Payment` 내부 필드를 보고 판단해야 하므로,  
  완전히 Validation 클래스로 빼면 오히려 결합이 늘어날 수 있다.

그래도 일부 공통 규칙을 분리하고 싶다면 (현재 `Payment` 는 `FINAL_TERMINAL_STATUSES` 상수로 직접 가드함):

```java
public final class PaymentStatePolicy {

  private PaymentStatePolicy() {}

  public static boolean isTerminal(PaymentStatus status) {
    return EnumSet.of(PaymentStatus.SUCCESS, PaymentStatus.FINAL_FAILED,
        PaymentStatus.CANCELED, PaymentStatus.REFUNDED).contains(status);
  }

  public static void validateStatusTransition(
      PaymentStatus current, Set<PaymentStatus> allowed, PaymentStatus target) { ... }
}
```

그리고 `Payment` 안에서는:

```java
private void validatePaymentStatusContains(Set<PaymentStatus> allowStatus, PaymentStatus target) {
    PaymentStatePolicy.validateStatusTransition(this.status, allowStatus, target);
}
```

정도로 **상태 관련 순수 로직만 위임**하는 정도가 적당하다.  
반대로, `paymentDeadlineAt`, `totalAmount`, `requestPgAmount` 등 다른 필드들을 보는 검증은  
엔티티 안에 두는 것이 낫다(도메인 규칙과 데이터가 같이 있어야 이해가 쉽다).

> 요약:  
> - “상태 값만 보면 되는 순수 정책”은 `PaymentStatus`/`PaymentStatePolicy` 로 뺄 수 있음  
> - “여러 필드를 같이 봐야 하는 도메인 규칙”은 `Payment` 안에 남기는 게 좋음

---

### 4. Toss Payments 외부 API 호출 & 재시도 전략

`PaymentFacade.confirmTossPayment(...)` 흐름:

```java
public ConfirmPaymentResponse confirmTossPayment(
    CustomUserDetails user, String orderNo, ConfirmPaymentRequest confirmPaymentRequest) {

  PaymentProcessContext context =
      PaymentProcessContext.fromConfirmPaymentRequest(
          user.getMemberId(), orderNo, confirmPaymentRequest);

  // 1. 결제 진행 상태로 변경 및 검증
  paymentInProgressUseCase.executeForPaymentConfirm(context);

  // 2. 토스페이먼츠 결제 승인 요청 및 결과 저장 (외부 API)
  context = paymentConfirmTossPaymentUseCase.execute(context);

  // 3. 결제 완료 처리 (계좌 입출금, 이벤트 발행)
  paymentCompleteUseCase.execute(context);

  return ConfirmPaymentResponse.complete(context.orderNo());
}
```

여기서 **2번 단계**가 외부 API(Toss) 호출이다.

#### 4.1 어떤 에러를 “재시도”할지 / “최종 실패”로 볼지

일반적으로:

- **재시도 대상 (transient)**:
  - 네트워크 타임아웃, 일시적 5xx, 토스 서버 장애 등
- **최종 실패 (business / 잘못된 요청)**:
  - 잘못된 paymentKey/orderNo, 이미 처리된 결제, 금액 불일치 등

재시도는 **횟수/시간을 제한**해야 하고,  
재시도 불가 또는 횟수 초과 시 **최종 실패**로 전환해 Saga 보상이 트리거되도록 한다.

#### 4.2 구현 방향

1. **Toss 호출 레이어에서 예외 분류**
   - `paymentConfirmTossPaymentUseCase.execute(context)` 내부에서 Toss API를 호출할 때:
     - `SocketTimeoutException`, `ConnectException`, HTTP 502/503 등 → **재시도 가능**으로 분류
     - HTTP 400/404, 토스 에러 코드(이미 처리된 결제, 금액 불일치 등) → **즉시 최종 실패**로 분류
   - 분류 결과에 따라:
     - 재시도 가능: UseCase에서 `Payment.updateFailureInfo(PaymentErrorCode, message)` (해당 코드가 `isFinalFailure() == false` 인 경우 → `FAILED` 전이) 또는 이벤트만 발행
     - 최종 실패: `Payment.updateFailureInfo(PaymentErrorCode, message)` (해당 코드가 `isFinalFailure() == true` 인 경우 → `FINAL_FAILED` 전이) + Outbox에 `PaymentFailedEvent` 발행 → `PaymentFailureUseCase` 에서 `PaymentFinalFailureEvent` 기록

2. **재시도 정책 설정 예시**
   - 최대 재시도 횟수: 2~3회
   - 백오프: 1초 → 2초 → 4초 (exponential backoff) 또는 고정 2초
   - 재시도는 **동일 요청(같은 paymentKey/orderId)** 에 대해서만 수행하고,  
     사용자가 "다시 결제하기"를 누르면 `Payment.changeToPending(paymentDeadlineAt)` 으로 마감일 갱신 후 PENDING으로 되돌려 새로 시도하는 플로우와 구분

3. **최종 실패로 전환하는 시점**
   - 재시도 횟수를 초과했을 때 (`PaymentErrorCode.PG_TOSS_MAX_RETRY_EXCEEDED` 등)
   - 비즈니스 에러(잘못된 요청, 이미 처리됨 등)를 받았을 때
   - 위 두 경우 모두 UseCase에서 `Payment.updateFailureInfo(errorCode, message)` 를 호출할 때  
     `PaymentErrorCode.isFinalFailure() == true` 인 코드를 넘기면 `FINAL_FAILED` 로 저장되고,  
     `PaymentFailedEvent` 발행 → `PaymentFailureUseCase` 가 `PaymentFinalFailureEvent` 를 Outbox에 남기면 Order/Inventory 가 보상 트랜잭션을 수행한다.

#### 4.3 설정 예시 (application.yml)

```yaml
payment:
  toss:
    confirm:
      max-retries: 3
      backoff-ms: 2000
      retryable-status-codes: 502,503,504
# 비즈니스 에러(4xx, 특정 에러 코드)는 재시도하지 않고 즉시 FINAL_FAILED
```

---

### 5. Saga 로깅을 위한 준비

나중에 AOP + `@SagaStep` 어노테이션으로 `[SAGA][OrderCreate][step=...][traceId=...]` Prefix 로그를 붙이려면:

1. **단계(step) 이름을 고정 문자열로 정의**
   - 예: `PAYMENT_IN_PROGRESS`, `PAYMENT_TOSS_CONFIRM`, `PAYMENT_COMPLETE`, `PAYMENT_FINAL_FAILED`, `COMPENSATE_ORDER` 등
   - Facade/UseCase의 **진입 메서드** 또는 **이벤트 핸들러**에 `@SagaStep(sagaName="OrderCreate", step="PAYMENT_TOSS_CONFIRM")` 처럼 붙인다.

2. **traceId 일관 사용**
   - API 진입 시 traceId를 MDC에 세팅하고,  
     Kafka 이벤트 발행 시 `TraceableEvent` / `EventUtils.extractTraceId()` 로 동일 traceId를 payload에 넣어두면  
     한 Saga의 전체 흐름을 Grafana에서 `traceId=xxx` 로 검색할 수 있다.

3. **로그 메시지 포맷**
   - 성공/실패 시 `[SAGA][{sagaName}][step={step}][traceId={traceId}] 시작/완료/실패` 형태로 통일하면  
     Loki/ELK에서 step별 필터링·집계가 쉽다.

---

### 6. PaymentFacade 3단계(결제 완료 처리)를 이벤트로 바꿀지

현재:

```java
// 3. 결제 완료 처리 (계좌 입출금, 이벤트 발행)
paymentCompleteUseCase.execute(context);
```

이 호출은 **Payment BC 내부**에서 계좌 잔고 차감 + (필요 시) Outbox 이벤트 발행까지 한 번에 처리하는 구조다.

- **이벤트로 분리해야 하는 경우**
  - 계좌 차감을 **다른 서비스(예: 별도 Ledger 서비스)** 가 담당하고,  
    Payment는 "결제 승인됐다"는 이벤트만 발행하고 끝내는 구조로 갈 때  
    → `PaymentApprovedEvent` 또는 `PaymentCompletedEvent` 를 발행하고,  
    Ledger(또는 동일 모듈 내 리스너)가 그 이벤트를 받아 계좌 차감을 수행하도록 바꾸면 된다.

- **한 Facade에서 같이 호출해도 되는 경우**
  - 계좌 입출금이 **같은 Payment 서비스/DB** 안에서 이루어지고,  
    "결제 완료"가 곧바로 "계좌 차감 + 완료 이벤트 1회 발행"으로 이어지는 현재처럼 **원자적으로 처리**하고 싶을 때  
  → `paymentCompleteUseCase.execute(context)` 를 그대로 두고,  
    이 UseCase 내부에서만 Outbox에 `PaymentCompletedEvent`(또는 Order 쪽이 기다리는 이벤트)를 한 번 발행하는 방식이면 충분하다.

정리하면, **다른 BC로 넘길 작업(Order/Inventory에 알림)** 만 이벤트로 하고,  
**Payment BC 내부의 계좌 차감**은 동기 UseCase 호출로 두는 현재 구조를 유지해도 된다.  
나중에 Ledger를 물리적으로 분리할 때 그때 "결제 승인 이벤트 → Ledger가 차감" 형태로 이벤트를 도입하면 된다.
