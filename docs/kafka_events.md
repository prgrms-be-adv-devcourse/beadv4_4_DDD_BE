# Kafka 도메인 이벤트 적용 가이드

## 적용 순서 (4단계)

1. **KafkaResolver에 토픽 등록**  
   자신의 모듈에 해당하는 TOPIC을 KafkaResolver에 등록한다.

2. **KafkaResolver에 키 등록**  
   각 이벤트별로 적용하고자 하는 key를 등록한다.  
   - 같은 topic 안에서 **동일한 key**이면 **같은 파티션**에서 **순차적으로** 메시지가 처리된다.

3. **모듈별 KafkaEventListener 생성**  
   각 모듈별로 KafkaEventListener를 만든다.

4. **리스너에서 topic·eventType 분기**  
   KafkaListener의 **topic**은 모듈별 기준이며, 세부 이벤트는 **eventType**으로 분기하여 **facade** 메서드를 호출한다.

---

## 1. KafkaResolver (토픽·키 등록)

### 토픽 등록

| 이벤트 타입 | 토픽 |
|-------------|------|
| MemberSignupEvent | member-events |
| PaymentMemberCreatedEvent, PaymentFailedEvent | payment-events |
| RefundRequestedEvent | order-events |
| SettlementCompletedPayoutEvent | settlement-events |

**새 이벤트 추가:** `resolveTopic(Object event)` 에 `if (event instanceof NewEvent) return "new-events-topic";` 추가.

### 키 등록

- 같은 key → 같은 파티션 → 순차 처리.
- **member-** : 회원 단위
- **payment-** : 결제/주문 단위
- **order-** : 주문 단위
- **settlement** : 정산 (단일 파티션)

**새 이벤트 추가:** `resolveKey(Object event)` 에 키 생성 로직 추가 (예: `"new-%s".formatted(e.id())`).

---

## 2. TraceableEvent 적용

이벤트에 **traceId**·**eventName**을 붙여 로깅·추적을 하려면 `TraceableEvent`를 구현한다.

### 인터페이스

```java
public interface TraceableEvent {
  String traceId();
  String eventName();  // default: getClass().getSimpleName()
}
```

### 적용 방법

1. **이벤트 record/클래스**에서 `implements TraceableEvent` 추가.
2. **traceId** 필드 추가. 생성 시 `EventUtils.extractTraceId()` 또는 MDC에서 주입.
3. **eventName()** 은 default 메서드로 클래스 이름 반환 → 구현 생략 가능.
4. **eventName 상수**가 필요하면 `public static final String EVENT_NAME = "PaymentMemberCreatedEvent";` + `eventName()` 에서 반환.

```java
public record PaymentMemberCreatedEvent(Long memberId, String traceId)
    implements TraceableEvent {

  public PaymentMemberCreatedEvent(Long memberId) {
    this(memberId, EventUtils.extractTraceId());
  }

  @Override
  public String eventName() {
    return EVENT_NAME;
  }

  public static final String EVENT_NAME = "PaymentMemberCreatedEvent";
}
```

- **DomainEventEnvelope** 생성 시: 이벤트가 TraceableEvent이면 `traceId()` 를 envelope에 넣어 Kafka까지 전달.
- **리스너 분기** 시: `EventClass.EVENT_NAME` 또는 `envelope.eventType()` 으로 문자열 대신 상수 사용 가능.

---

## 3. 모듈별 KafkaEventListener

### 패턴

- **topic** = 모듈별 1개 (KafkaResolver와 동일).
- **groupId** = 모듈/서비스명 (같은 이벤트를 여러 모듈이 구독할 수 있음).
- **파라미터** = `DomainEventEnvelope`.
- **분기** = `envelope.eventType()` → `jsonConverter.deserialize(payload, EventClass.class)` → **facade** 호출.

```java
@KafkaListener(topics = "member-events", groupId = "payment-service")
@Transactional(propagation = REQUIRES_NEW)
public void handleMemberEvent(DomainEventEnvelope envelope) {
  switch (envelope.eventType()) {
    case "MemberSignupEvent" -> {
      MemberSignupEvent event = jsonConverter.deserialize(
          envelope.payload(), MemberSignupEvent.class);
      paymentFacade.createPaymentMember(paymentMapper.toPaymentMemberDto(event));
    }
    default -> { /* 무시 */ }
  }
}
```

### 새 토픽/이벤트 구독 시

1. 구독할 토픽에 `@KafkaListener(topics = "xxx-events", groupId = "my-service")` 메서드 추가.
2. `envelope.eventType()` 으로 분기 후 `jsonConverter.deserialize(envelope.payload(), EventClass.class)`.
3. 해당 모듈의 **facade** 메서드 호출.
