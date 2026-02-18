## 결제 Saga & 상태머신 설계 가이드

### 1. 목표

- `Payment` 엔티티를 **명시적인 상태머신**으로 사용해 결제 진행/실패/보상 흐름을 일관성 있게 관리한다.
- 외부 PG(Toss Payments) 호출을 포함한 **각 단계별 재시도/최종 실패 처리 규칙**을 정한다.
- 나중에 **Saga 로깅(AOP, traceId, Grafana)** 을 쉽게 붙일 수 있도록 단계/이벤트를 명확히 한다.

---

### 2. Payment 상태머신 정리

#### 2.1 상태 집합 (`PaymentStatus`)

```java
public enum PaymentStatus {
  PENDING,          // 결제 대기
  IN_PROGRESS,      // 결제 진행 (PG 승인 전/중)
  APPROVED,         // PG 승인 완료, 내부 처리 대기
  CANCELED,         // 결제 취소
  FAILED,           // 결제 실패 (재시도 가능)
  FINAL_FAILED,     // 최종 결제 실패 (재시도 불가, Saga 보상 트리거)
  SUCCESS,          // 결제 완료
  REFUND_REQUESTED, // 환불 요청
  REFUNDED;         // 환불 완료
}
```

#### 2.2 전이(Transition) 개념

`Payment` 엔티티는 이미 아래와 같이 핵심 전이 메서드를 가지고 있다.

- `changeInProgress()` : `PENDING/FAILED` → `IN_PROGRESS`
- `approveTossPayment(...)` : `IN_PROGRESS` → `APPROVED`
- `changeToSuccess()` : `IN_PROGRESS/APPROVED` → `SUCCESS`
- `failedPayment(...)` : (어디서든) → `FAILED` or `FINAL_FAILED`
- `initPayment(...)` : `PENDING/FAILED` 에서 재시도 위한 초기화
- `syncToInProgress() / syncToApproved() / syncToCanceled()` : 외부 이벤트에 동기화

이 메서드들이 **상태머신의 “간선”** 역할을 하고,  
`validate*` 메서드들이 **불법 전이를 막는 가드(guard)** 역할을 한다.

> 상태머신의 “두뇌”는 `Payment` 엔티티에 두고,  
> 외부에서는 **반드시 전이 메서드만 사용해서 상태를 바꾸는 것**이 핵심이다.

---

### 3. 상태 관련 validation 정리 전략

현재 `Payment` 안에 아래와 같은 validation이 섞여있다:

- 상태 기반:
  - `validatePaymentStatus(...)`
  - `validatePaymentStatusContains(...)`
  - `validateNotTerminalStatus()`
  - `isTerminalStatus()` (`FINAL_TERMINAL_STATUSES`)
  - `isRetryable()`
  - `validateCanChangeToInProgress()` (상태 + 마감일)
- 시간/금액 기반:
  - `validatePaymentDeadline()`
  - `validateTotalAmount(...)`
  - `validateChargeAmount(...)`

#### 3.1 같은 파일 안에서 가독성 높이는 방법

1. **섹션별로 묶어두기**
   - 코드 순서를 크게 4 블록으로 나눈다:
     1. 필드/생성자
     2. **상태 전이 메서드 (public)**  
        - `changeInProgress`, `approveTossPayment`, `changeToSuccess`, `failedPayment`, `initPayment`, `syncTo*` 등
     3. **상태 관련 validation (private)**  
        - `isTerminalStatus`, `isRetryable`, `validatePaymentStatus*`, `validateNotTerminalStatus`, `validateCanChangeToInProgress`
     4. **값/마감일/금액 관련 validation (private)**  
        - `validateTotalAmount`, `validatePaymentDeadline`, `validateChargeAmount`

   - 예시(개념):

   ```java
   // 1) public API: 상태 전이 메서드들
   public void changeInProgress() { ... }
   public void approveTossPayment(...) { ... }
   public void changeSuccess() { ... }
   public void failedPayment(...) { ... }
   public void failedTossPayment(...) { ... }
   public void initPayment(...) { ... }
   public void syncToInProgress() { ... }
   public void syncToApproved() { ... }
   public void syncToCanceled() { ... }

   // 2) 상태 관련 validation
   private boolean isTerminalStatus() { ... }
   private boolean isRetryable() { ... }
   private void validateNotTerminalStatus() { ... }
   private void validatePaymentStatus(...) { ... }
   private void validatePaymentStatusContains(...) { ... }
   private void validateCanChangeToInProgress() { ... }

   // 3) 값/마감일/금액 관련 validation
   private static void validateTotalAmount(BigDecimal amount) { ... }
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

그래도 일부 공통 규칙을 분리하고 싶다면:

```java
public final class PaymentStatePolicy {

  private PaymentStatePolicy() {}

  public static boolean isTerminal(PaymentStatus status) { ... }

  public static boolean canRetry(PaymentStatus status) { ... }

  public static void validateStatusTransition(
      PaymentStatus current, Set<PaymentStatus> allowed, PaymentStatus target) { ... }
}
```

그리고 `Payment` 안에서는:

```java
private boolean isTerminalStatus() {
    return PaymentStatePolicy.isTerminal(this.status);
}

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
     - 재시도 가능: `Payment.failedPayment(..., FAILED)` + (선택) 재시도 스케줄/큐
     - 최종 실패: `Payment.failedPayment(..., FINAL_FAILED)` + Outbox에 `PaymentFinalFailureEvent` 기록

2. **재시도 정책 설정 예시**
   - 최대 재시도 횟수: 2~3회
   - 백오프: 1초 → 2초 → 4초 (exponential backoff) 또는 고정 2초
   - 재시도는 **동일 요청(같은 paymentKey/orderId)** 에 대해서만 수행하고,  
     사용자가 "다시 결제하기"를 누르면 `initPayment` 후 새로 시도하는 플로우와 구분

3. **최종 실패로 전환하는 시점**
   - 재시도 횟수를 초과했을 때
   - 비즈니스 에러(잘못된 요청, 이미 처리됨 등)를 받았을 때
   - 위 두 경우 모두 `Payment.failedPayment(errorCode, message)` 를 호출할 때  
     `errorCode.isFinalFailure() == true` 인 코드를 넘기면 `FINAL_FAILED` 로 저장되고,  
     이 시점에서 Outbox에 `PaymentFinalFailureEvent` 를 남기면 Order/Inventory 가 보상 트랜잭션을 수행한다.

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
