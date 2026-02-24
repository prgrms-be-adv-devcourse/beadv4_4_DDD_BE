# 토스 PG 외부 API 실패 처리 및 보상 트랜잭션

## 1. 목표

- 토스페이먼츠 등 **외부 PG API 호출 실패** 시 재시도/최종 실패를 구분해 처리한다.
- **재시도 N회(예: 3회) 초과 시 최종 실패**로 간주하고, **보상 트랜잭션(Saga)** 을 트리거한다.
- 외부 API 실패 처리에서 쓸 수 있는 패턴과 선택지를 정리한다.

---

## 2. 적용된 구조: 토스 PG 실패 → 보상 트랜잭션

### 2.1 RestTossPaymentClient (재시도 정책)

- **Toss PG 전용** 재시도를 위해 `@Retryable` 을 **직접** 선언 (공용 `@RetryOnExternalApiFailure` 미사용).
- 설정:
  - `retryFor`: `TossConfirmRetryableException`, `ResourceAccessException`
  - `noRetryFor`: `TossConfirmFailedException`
  - `maxAttempts = 3`, `backoff = 500ms × 2^n`
- 동작:
  - **502/503/504** 또는 **네트워크/타임아웃**(`ResourceAccessException`) → `TossConfirmRetryableException` 또는 `ResourceAccessException` 발생 → **최대 3회** 재시도.
  - **4xx, 500**, body null, 기타 → `TossConfirmFailedException` 발생 → **재시도 없이** 즉시 실패.
- 3회 모두 실패한 뒤에만 예외가 UseCase 로 전파된다.

### 2.2 PaymentConfirmTossPaymentUseCase (실패 시 이벤트 발행)

- `tossPaymentClient.confirmPayment(tossReq)` 호출 후 예외별 처리:
  - **TossConfirmRetryableException**: `@Retryable` 이 3회 재시도한 뒤 전파됨 → **재시도 소진**으로 간주.  
    `handleTossFailure(context, PG_TOSS_MAX_RETRY_EXCEEDED, e.getMessage())` 호출 후 예외 재발생.
  - **TossConfirmFailedException**: Client 에서 이미 구분된 비즈니스/즉시 실패.  
    `handleTossFailure(context, e.getErrorCode(), tossMessage)` 호출 후 예외 재발생.
  - **Exception** (그 외): `payment.updatePgFailureInfo(500, message)` +  
    `handleTossFailure(context, PG_UNKNOWN_ERROR, e.getMessage())` 후 예외 재발생.
- `handleTossFailure` 는 **PaymentInProgressUseCase.handleFailure** 와 동일하게 **PaymentFailedEvent** 만 `eventPublisher.publish(event)` 로 발행한다.  
  (UseCase 에서 Payment 상태를 직접 바꾸지 않고, 이벤트 수신 측에서 처리한다.)

### 2.3 PaymentFailureUseCase (실패 처리 및 보상 트리거)

- 발행된 **PaymentFailedEvent** 는 리스너(Kafka 또는 in-process)를 통해 **PaymentFailureUseCase.execute(event)** 로 전달된다.
- PaymentFailureUseCase:
  1. `PaymentErrorCode.fromCode(event.errorCode())` 로 에러 코드 복원.
  2. `payment.updateFailureInfo(error, event.failureMessage())` → `error.isFinalFailure()` 이면 **FINAL_FAILED**, 아니면 **FAILED** 로 전이.
  3. **`error.isFinalFailure() == true`** 이면 **PaymentFinalFailureEvent** 발행 → Order/Inventory 가 보상 트랜잭션(주문 취소, 재고 복구 등) 수행.

### 2.4 PaymentErrorCode (최종 실패 구분)

- 토스 PG 관련 코드:
  - **PG_TOSS_CONFIRM_FAILED** (PAYMENT_4010): 비즈니스/즉시 실패(4xx, 500 등). `FINAL_FAILURE_CODES` 포함.
  - **PG_TOSS_MAX_RETRY_EXCEEDED** (PAYMENT_4011): 재시도 3회 소진. `FINAL_FAILURE_CODES` 포함.
  - **PG_UNKNOWN_ERROR** (PAYMENT_4099): 그 외 예외. `FINAL_FAILURE_CODES` 포함.
- 위 코드들은 `isFinalFailure() == true` 이므로, PaymentFailureUseCase 에서 **PaymentFinalFailureEvent** 가 발행되고 보상이 트리거된다.

### 2.5 전체 흐름 요약

1. **Client**: 502/503/504 또는 네트워크 오류 → 재시도용 예외 → **최대 3회 재시도**.
2. **3회 모두 실패** 시 `TossConfirmRetryableException` 이 UseCase 로 전파.
3. **UseCase**: `PaymentFailedEvent`(errorCode=PG_TOSS_MAX_RETRY_EXCEEDED 등) **발행** 후 예외 재발생.
4. **리스너** → **PaymentFailureUseCase.execute(event)** 호출.
5. **PaymentFailureUseCase**: `payment.updateFailureInfo()` 로 **FINAL_FAILED** 전이, **PaymentFinalFailureEvent** 발행 → **보상 트랜잭션** 수행.

---

## 3. 외부 API 실패 처리에서 할 수 있는 것들

### 3.1 재시도 (Retry)

| 항목 | 설명 |
|------|------|
| **목적** | 일시적 오류(네트워크, 502/503/504)에만 재시도해 성공 확률을 높인다. |
| **구현** | 토스 PG 전용은 `RestTossPaymentClient.confirmPayment()` 에 **@Retryable 직접 선언** (retryFor: TossConfirmRetryableException, ResourceAccessException). 공용 정책이 필요하면 `@RetryOnExternalApiFailure` 사용. |
| **설정** | 최대 시도 횟수(예: 3), 백오프(고정/지수), 재시도 대상 예외/HTTP 코드. |
| **주의** | 비즈니스 에러(잘못된 paymentKey, 금액 불일치 등)는 재시도하면 안 되므로 `noRetryFor` 로 제외. |

### 3.2 재시도 가능 vs 최종 실패 구분

| 구분 | 재시도(transient) | 최종 실패(business / exhausted) |
|------|-------------------|----------------------------------|
| **예** | 타임아웃, 502/503/504, 연결 실패 | 잘못된 요청(4xx), 이미 처리된 결제, 금액 불일치, **재시도 N회 초과** |
| **Payment 상태** | `FAILED` (재시도 가능) | `FINAL_FAILED` |
| **다음 동작** | 사용자 재시도 또는 스케줄 재시도 | **보상 트랜잭션** (Order/Inventory 취소·복구) |

- Toss 응답/예외를 **에러 코드·HTTP status** 로 분류하고,  
  “재시도 소진” 은 **같은 요청에 대한 N회 실패** 로 정의해 **최종 실패**로 넘기면 된다.

### 3.3 최종 실패 시 보상 트랜잭션 (Saga)

| 항목 | 설명 |
|------|------|
| **트리거** | UseCase 는 **PaymentFailedEvent** 만 발행. **PaymentFailureUseCase** 가 이벤트 수신 시 `payment.updateFailureInfo()` 로 **FINAL_FAILED** 전이 후 **PaymentFinalFailureEvent** 발행(Outbox 권장). |
| **소비** | Order 쪽에서 PaymentFinalFailureEvent 수신 시 주문 취소, Inventory 에서는 재고/예약 복구 등. |
| **참고** | [payment-saga-and-state-machine.md](payment-saga-and-state-machine.md), [idempotency.md](./idempotency.md). |

### 3.4 타임아웃

| 항목 | 설명 |
|------|------|
| **목적** | 외부 API가 무한 대기하지 않도록 제한. |
| **구현** | RestClient/WebClient 의 `timeout` 설정, 또는 커넥션/읽기 타임아웃. |
| **효과** | 타임아웃 시 보통 `ResourceAccessException` 등으로 나와, 토스 Client 에서는 재시도 대상 예외로 처리됨. |

### 3.5 에러 매핑 및 로깅

| 항목 | 설명 |
|------|------|
| **에러 매핑** | HTTP status / 토스 에러 코드 → `PaymentErrorCode` 매핑. `isFinalFailure()` 로 즉시 최종 실패 여부 결정. |
| **로깅** | paymentKey, orderId, 시도 횟수, 최종 실패 사유 등을 구조화 로그로 남기면 장애 분석·감사에 유리. |

---

## 4. 정리 (결제 모듈 적용 내용)

1. **재시도**: `RestTossPaymentClient.confirmPayment()` 에 **@Retryable 직접 선언**.  
   재시도: `TossConfirmRetryableException`, `ResourceAccessException` / 재시도 안 함: `TossConfirmFailedException` / 3회, 백오프 500ms×2^n.
2. **실패 시 이벤트 발행**: `PaymentConfirmTossPaymentUseCase` 에서 Toss 관련 예외 catch 시  
   **PaymentFailedEvent** 만 `eventPublisher.publish()` 로 발행 (PaymentInProgressUseCase 와 동일 패턴).  
   재시도 소진 → `PG_TOSS_MAX_RETRY_EXCEEDED`, 즉시 실패 → `TossConfirmFailedException.getErrorCode()`, 그 외 → `PG_UNKNOWN_ERROR`.
3. **실패 처리 및 보상**: 발행된 **PaymentFailedEvent** 는 리스너를 통해 **PaymentFailureUseCase.execute(event)** 에서 처리.  
   `payment.updateFailureInfo(error, message)` 로 FINAL_FAILED 전이 후, **PaymentFinalFailureEvent** 발행 → Order/Inventory 보상 트랜잭션.
4. (선택) 회로 차단, 타임아웃 튜닝, 에러 코드 확장은 필요 시 단계적으로 추가.

현재 “토스 외부 API 실패 → Client 3회 재시도 → UseCase 에서 PaymentFailedEvent 발행 → PaymentFailureUseCase 에서 FINAL_FAILED 전이 및 PaymentFinalFailureEvent 발행 → 보상 트랜잭션” 이 일관되게 연결되어 있다.
