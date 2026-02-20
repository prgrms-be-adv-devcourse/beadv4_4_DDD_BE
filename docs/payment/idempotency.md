## 결제 모듈 멱등성(Idempotency) 설계

### 1. 개요

결제 모듈은 **같은 요청/이벤트/웹훅이 여러 번 들어와도, 비즈니스 효과는 한 번만 나도록** 설계했다.  
아래는 주요 엔티티별로 어떤 키를 기준으로 멱등성을 보장하는지 정리한 것이다.

- **명령/흐름 멱등성**: `Payment`, `PaymentStatus` 상태머신
- **이벤트 소비 멱등성**: `PaymentInboxEvent`
- **이벤트 발행 멱등성**: `PaymentOutboxEvent`
- **PG 웹훅 멱등성**: `PaymentTossWebhookLog`
- **계좌 입출금 멱등성**: `PaymentAccount` + `PaymentAccountLog`

---

### 2. Payment 상태머신 기반 멱등성

#### 2.1 비즈니스 키: `PaymentId(memberId, orderNo)`

`Payment` 엔티티는 `PaymentId(memberId, orderNo)` + 유니크 제약을 사용한다.  
동일 회원·주문에 대해 여러 번 결제 생성/확정 로직을 태워도 **항상 같은 Payment 레코드**를 바라보게 된다.

```java
// 개념 예시: 존재하면 재사용, 없으면 생성
PaymentId paymentId = PaymentId.of(memberId, orderNo);

Payment payment = paymentRepository
    .findById(paymentId)
    .orElseGet(() -> Payment.create(
        paymentId,
        orderId,
        totalAmount,
        paymentDeadlineAt,
        providerType,
        paymentPurpose));
```

#### 2.2 상태머신 + 최종 상태(terminal) 체크

`PaymentStatus` 는 `SUCCESS`, `FINAL_FAILED`, `CANCELED`, `REFUNDED` 를 **최종 상태(terminal)** 로,  
`PENDING`, `FAILED` 를 **재시도 가능 상태**로 구분한다.

동일 결제에 대해 확정/완료 로직이 여러 번 호출되어도:

- 이미 `SUCCESS` 또는 `FINAL_FAILED` 면 **추가 상태 변경을 막고** 멱등 응답을 줄 수 있다.

```java
// 개념 예시
public void confirmPaymentIdempotent(PaymentId id, TossPaymentsConfirmResponse tossRes) {
    Payment payment = paymentRepository.findById(id).orElseThrow(...);

    if (payment.getStatus().isTerminal()) {
        // 이미 최종 상태 → 멱등 응답
        return;
    }

    payment.approveTossPayment(tossRes);
    payment.changeSuccess();
}
```

이렇게 **비즈니스 키 + 상태머신**으로 “같은 결제를 여러 번 처리해도 결과는 한 번만”이 되도록 만든다.

---

### 3. Inbox 기반 이벤트 소비 멱등성 (`PaymentInboxEvent`)

외부(Boundary 밖)에서 들어오는 Kafka 이벤트는 `PaymentInboxEvent` 로 한 번 더 저장한 뒤 처리한다.

- 컬럼: `eventId` 에 **유니크 제약** (`uniqueConstraints = "event_id"`)
- 패턴: “Inbox row INSERT 성공 시에만 실제 비즈니스 로직 실행”

```java
@Entity
@Table(
    name = "payment_inbox_event",
    uniqueConstraints = {@UniqueConstraint(columnNames = "event_id")})
public class PaymentInboxEvent { ... }
```

개념적인 소비 코드:

```java
@Transactional
public void handleOrderEvent(IdempotentOrderEvent event) {
    try {
        PaymentInboxEvent inbox =
            PaymentInboxEvent.create(event.getEventId(), event.getTopic(), event.getPayload(), event.getTraceId());
        paymentInboxEventRepository.save(inbox);
    } catch (DataIntegrityViolationException e) {
        // eventId 유니크 위반 → 이미 처리한 이벤트 → 멱등하게 무시
        return;
    }

    // 여기부터가 실제 비즈니스 처리
    processOrderPayment(event);
}
```

→ 같은 Kafka 메시지가 재전송되더라도, **eventId 기준으로 한 번만 처리**된다.

---

### 4. Outbox 기반 이벤트 발행 멱등성 (`PaymentOutboxEvent`)

#### 4.1 eventId + 유니크 제약

`PaymentOutboxEvent` 는 `eventId` 에 유니크 제약을 둔다.

- eventId 생성 규칙 예시:
  - `eventId = aggregateType + ":" + aggregateId + ":" + eventType`
  - 예: `PAYMENT:12345:PaymentCompleted`

```java
@Entity
@Table(
    name = "payment_outbox_event",
    uniqueConstraints = {@UniqueConstraint(columnNames = "event_id")})
public class PaymentOutboxEvent { ... }
```

Outbox 저장 시점에 eventId 를 세팅하고, 중복 INSERT 는 예외로 무시하는 패턴:

```java
@Transactional
public void saveToOutbox(Object event) {
    KafkaPublishTarget target = kafkaResolver.resolve(event);
    String payload = jsonConverter.serialize(event);

    String eventType = event.getClass().getSimpleName();
    String eventId = target.aggregateType() + ":" + target.aggregateId() + ":" + eventType;

    PaymentOutboxEvent outboxEvent =
        PaymentOutboxEvent.create(
            target.aggregateType(),
            target.aggregateId(),
            eventType,
            target.topic(),
            payload,
            target.traceId(),
            eventId);

    try {
        paymentOutboxStore.store(outboxEvent);
    } catch (DataIntegrityViolationException e) {
        // 동일 eventId Outbox 이미 존재 → 멱등하게 무시
    }
}
```

→ 같은 비즈니스 이벤트에 대해 Outbox row 는 **항상 최대 1개**만 유지된다.

#### 4.2 Poller + 락을 통한 중복 발행 방지

OutboxPoller 는 `status = PENDING` 인 이벤트를 읽어와 Kafka 로 전송하고,  
DB 락(`SELECT ... FOR UPDATE SKIP LOCKED` 등)이나 `@Version` 을 사용해 **동시에 두 번 발행하지 않도록** 보장한다.

핵심은:

- **생성 시점**: `eventId` 유니크 제약으로 Outbox row 중복 생성 방지
- **발행 시점**: 비관적/낙관적 락으로 Outbox row 상태 전이(`PENDING → SENT/FAILED`) 중복 방지

---

### 5. Toss 웹훅 멱등성 (`PaymentTossWebhookLog`)

Toss Payments 웹훅은 같은 이벤트를 여러 번 보낼 수 있기 때문에,  
`PaymentTossWebhookLog` 로 수신 이력을 저장하고 **`transmissionId` 유니크**로 구분한다.

```java
@Entity
@Table(name = "payment_toss_webhook_log")
public class PaymentTossWebhookLog {
  @Column(nullable = false, unique = true)
  private String transmissionId;
  ...
}
```

개념적인 처리 흐름:

```java
@Transactional
public void handleTossWebhook(TossWebhookRequest req) {
    String transmissionId = req.transmissionId();

    Optional<PaymentTossWebhookLog> existing =
        paymentTossWebhookLogRepository.findByTransmissionId(transmissionId);

    if (existing.isPresent() && existing.get().getStatus().isTerminal()) {
        // 이미 성공/실패로 끝난 웹훅 → 멱등하게 무시
        return;
    }

    PaymentTossWebhookLog log =
        existing.orElseGet(() -> paymentTossWebhookLogRepository.save(
            PaymentTossWebhookLog.create(
                transmissionId,
                req.transmissionTime(),
                req.retryCount(),
                req.eventType(),
                req.payload())));

    // 비즈니스 처리 후 markSuccess/markFailed
}
```

→ Toss 가 같은 웹훅을 여러 번 보내도, **transmissionId 기준으로 한 번만 처리**된다.

---

### 6. 계좌 입출금 멱등성 (`PaymentAccount` + `PaymentAccountLog`)

결제 계좌 입출금은 `PaymentAccountLog` 의 `referenceId` / `referenceType` 으로  
“어떤 레퍼런스(예: 주문, 결제)에 대해 몇 번 처리했는지”를 추적한다.

```java
public class PaymentAccountLog {
  private Long referenceId;          // ex) orderId, paymentId
  private ReferenceType referenceType; // ex) ORDER, PAYMENT, MEMBER ...
}
```

“주문 1건당 계좌 차감은 한 번만” 이라는 정책은 다음과 같이 구현할 수 있다:

```java
@Transactional
public void debitForOrder(PaymentAccount account, Long orderId, BigDecimal amount) {
    boolean alreadyDebited =
        paymentAccountLogRepository.existsByReferenceIdAndReferenceType(
            orderId, ReferenceType.ORDER);

    if (alreadyDebited) {
        // 이미 해당 주문으로 차감된 이력이 있음 → 멱등하게 무시
        return;
    }

    account.debit(
        amount,
        PaymentEventType.ORDER_PAYMENT,
        orderId,
        ReferenceType.ORDER);
}
```

DB 레벨에서 `(referenceId, referenceType, eventType)` 유니크 인덱스를 추가하면,  
동시 호출에도 한 번만 로그가 쌓이도록 더 강하게 보장할 수 있다.

---

### 7. 정리

결제 모듈의 멱등성은 **단일 기법이 아니라, 여러 계층의 조합**으로 확보한다.

- **엔티티/도메인 계층**: `PaymentId` + 상태머신, 최종 상태 체크
- **메시지 인입 계층**: `PaymentInboxEvent.eventId` 유니크
- **메시지 발행 계층**: `PaymentOutboxEvent.eventId` 유니크 + Poller 락(@Version/비관적 락)
- **외부 웹훅 계층**: `PaymentTossWebhookLog.transmissionId` 유니크
- **계좌/잔고 계층**: `PaymentAccountLog.referenceId/referenceType` 기반 멱등 입출금

이렇게 각 계층에서 **자기 책임 범위 안에서 “같은 일을 여러 번 시도해도, 결과는 한 번만”** 이 되도록 설계해  
전체 결제 플로우의 멱등성을 보장한다.

