# Inbox 패턴

## 개요

Consumer 측에서 **수신한 이벤트를 DB에 기록**해, 같은 이벤트가 재전송돼도 **한 번만 도메인 로직을 실행**하도록 하는 패턴(멱등 처리).

- **Outbox**: 우리가 **보내는** 이벤트를 DB에 먼저 기록 후 Kafka 발행.
- **Inbox**: 우리가 **받은** 이벤트를 DB에 먼저 기록 후, 이미 있으면 스킵·없으면 저장하고 도메인 로직 실행.

Kafka는 at-least-once 전달이 가능하므로, 동일 메시지가 다시 올 수 있다. Inbox에 `eventId`를 유니크로 저장해 두고, 이미 존재하면 재처리하지 않는다.

---

## 처리 흐름

```text
Kafka 메시지 수신
       │
       ▼
┌─────────────────────────────────────────────┐
│  트랜잭션 (예: @Transactional(REQUIRES_NEW))  │
│  1. InboxRecorder.tryRecord(eventId, ...)   │
│     - 이미 eventId 존재 → false 반환 (스킵)    │
│     - 없음 → INSERT 후 true 반환             │
│  2. true일 때만 도메인 로직 실행              │
│  커밋                                        │
└─────────────────────────────────────────────┘
       │
       ▼
Kafka offset commit (afterCommit에서 ack)
```

- `tryRecord`가 **true** → 최초 수신 → 도메인 로직 실행.
- `tryRecord`가 **false** → 이미 처리된 이벤트 → 아무 작업 없이 스킵.

---

## InboxEvent 엔티티 (예: PaymentInboxEvent)

```text
필드         | 타입           | 설명
----------------------------------------------------------
id           | Long           | PK
event_id     | String (UK)    | 이벤트 고유 ID (Producer/Envelope의 eventId)
topic        | String         | 수신 토픽
payload      | LONGTEXT       | JSON 페이로드
trace_id     | String         | 분산 추적 ID (없으면 null)
received_at  | LocalDateTime  | 수신 시각
```

- **event_id** 에 유니크 제약을 걸어 동일 이벤트 중복 저장 방지.
- **trace_id** 저장 시 Producer와 동일한 trace로 수신·처리 로그를 묶을 수 있음.

---

## global inbox 패키지 클래스 (common)

`com.modeunsa.global.kafka.inbox` 에 정의된 공통 계약. 각 바운디드 컨텍스트(payment 등)가 구현해 사용한다.

### InboxEventView (interface)

Inbox 행의 읽기 전용 뷰. 엔티티나 프로젝션이 구현한다.

| 메서드 | 설명 |
|--------|------|
| getId() | PK |
| getEventId() | 멱등 키 (유니크) |
| getTopic() | 수신 토픽 |
| getPayload() | 페이로드 |
| getTraceId() | 분산 추적 ID (없으면 null) |

---

### InboxRecorder (interface)

수신 이벤트를 Inbox에 **한 번만** 기록하는 계약.

```java
boolean tryRecord(String eventId, String topic, String payload, String traceId);
```

- **반환값**: 이미 존재하면 `false`, 새로 기록하면 `true`.
- **true일 때만** 같은 트랜잭션 안에서 도메인 로직을 실행하면 멱등이 보장된다.
- `Propagation.MANDATORY` 로 두어, 호출 측 트랜잭션 안에서만 호출되도록 구현하는 것을 권장.

---

## payment 서비스 구현

### PaymentInboxEvent (entity)

- 테이블: `payment_inbox_event`, `eventId` 유니크.
- `InboxEventView` 구현.
- 정적 팩토리: `PaymentInboxEvent.create(eventId, topic, payload, traceId)`.

### Port (out)

| 인터페이스 | 역할 |
|------------|------|
| PaymentInboxReader | `existsByEventId(String eventId)` — 이미 수신한 이벤트인지 조회 |
| PaymentInboxStore | `store(PaymentInboxEvent)` — Inbox 행 저장 |

### Adapter (persistence)

| 클래스 | 역할 |
|--------|------|
| PaymentInboxRepository | JpaRepository. save. |
| PaymentInboxQueryRepository | Querydsl로 `countByEventId(eventId)` 제공. |
| JpaPaymentInboxReader | PaymentInboxReader 구현. countByEventId > 0 이면 존재. |
| JpaPaymentInboxStore | PaymentInboxStore 구현. repository.save 위임. |

### PaymentInboxRecorder (app/inbox)

- **InboxRecorder** 구현.
- `PaymentInboxReader.existsByEventId(eventId)` 로 존재 여부 확인 후, 없으면 `PaymentInboxEvent.create(...)` 로 생성해 `PaymentInboxStore.store(...)` 호출.
- `@Transactional(propagation = Propagation.MANDATORY)` 로 기존 트랜잭션 안에서만 동작하도록 함.

### Kafka 리스너에서 사용

- 각 `@KafkaListener` 메서드에서 `@Transactional(propagation = REQUIRES_NEW)` 로 트랜잭션 경계.
- `inboxRecorder.tryRecord(envelope.eventId(), envelope.topic(), envelope.payload(), envelope.traceId())` 호출.
- **true**를 반환하면(최초 수신) 이벤트 타입별로 payload 역직렬화 후 Facade/유스케이스 호출.
- **false**를 반환하면(이미 처리됨) 도메인 로직 없이 스킵.
- `TransactionSynchronization.afterCommit()` 에서 `ack.acknowledge()` 호출해 커밋 후에만 offset 커밋.

