# 정산 시스템 (Settlement)

## 개요

월 정산 배치 파이프라인 설계 및 분산 환경에서의 데이터 정합성/멱등성 보장 시스템 구현

**기술 스택**: `Spring Boot 4.0` `Spring Batch` `Spring Kafka` `JPA/QueryDSL` `MySQL` `Java 21` `DDD` `Hexagonal Architecture`

---

## 아키텍처

### 헥사고날 아키텍처 기반 패키지 구조

```
settlement-service/src/main/java/.../settlement/
├── domain/
│   ├── entity/
│   │   ├── Settlement.java               # 정산 Aggregate Root
│   │   ├── SettlementItem.java           # 정산 항목
│   │   ├── SettlementCandidateItem.java  # 정산 후보 항목 (구매 확정 주문)
│   │   ├── SettlementMember.java         # 판매자 정보
│   │   └── SettlementOutboxEvent.java    # Outbox 이벤트 엔티티
│   ├── types/
│   │   ├── SettlementStatus.java         # PENDING, PROCESSING, COMPLETED
│   │   └── SettlementEventType.java
│   ├── PayoutAmounts.java                # 수수료 계산 VO
│   └── policy/
│       └── SettlementPolicy.java         # 수수료율 정책
├── app/                                  # UseCase (Application Layer)
│   ├── SettlementFacade.java
│   ├── outbox/
│   │   ├── SettlementOutboxPoller.java
│   │   └── SettlementOutboxPublisher.java
│   └── ...
├── in/                                   # Inbound Adapters
│   ├── batch/                            # Spring Batch
│   ├── SettlementKafkaEventListener.java # Kafka Consumer
│   └── ApiV1SettlementController.java    # REST API
└── out/                                  # Outbound Adapters
    └── persistence/                      # JPA Repository 구현체
```

### 이벤트 흐름

```
주문 확정 (Order Service)
  ↓ OrderPurchaseConfirmedEvent (Kafka)
SettlementKafkaEventListener
  ↓
SettlementCandidateItem 생성 (collectedAt = null)

──────────────────────────────────

매일 03:00 배치
  ↓
collectItemsAndCalculatePayoutsJob
  └── claim → addItems → save (멱등성 3단계 적용)

──────────────────────────────────

매월 25일 04:00 배치
  ↓
monthlySettlementJob (2단계 파이프라인)
  ├── Step 1: PENDING → PROCESSING
  └── Step 2: PROCESSING → COMPLETED
      ↓ SettlementCompletedPayoutEvent
      Outbox 저장 (same transaction)
      ↓ Outbox Poller (5초 주기)
      Kafka: settlement-events topic
```

---

## 주요 구현

### 1. 월 정산 배치 파이프라인

Spring Batch 기반 2단계 파이프라인으로 명확한 상태 경계를 설계해 배치 실패 시 복구 범위를 한정.

```
monthlySettlementJob
├── Step 1: reserveMonthlySettlementStep
│   └── PENDING 정산 조회 → batchExecutionId 부여 → PROCESSING 전환
│
└── Step 2: completeMonthlySettlementStep
    ├── completePayout() 호출 → COMPLETED 전환 + payoutAt 기록
    └── SettlementCompletedPayoutEvent 발행 → Outbox 저장
```

**배치 실패 자동 롤백**: `JobExecutionListener.afterJob()`에서 실패 감지 시 해당 `batchExecutionId`의 PROCESSING 상태를 전부 PENDING으로 복구 → 재실행 가능

```java
public void afterJob(JobExecution jobExecution) {
    if (!jobExecution.getStatus().isUnsuccessful()) return;

    Long batchExecutionId = jobExecution.getId();
    List<Settlement> processing =
        settlementRepository.findByBatchExecutionIdAndStatus(
            batchExecutionId, SettlementStatus.PROCESSING);

    processing.forEach(Settlement::rollbackToPending);
}
```

**배치 상태 관리**:

```
PENDING
  ↓ (Step 1)
PROCESSING  ←── 실패 시 JobExecutionListener가 자동 롤백
  ↓ (Step 3)
COMPLETED   (이미 완료된 경우 재처리 방지 - 멱등)
```

---

### 2. Transactional Outbox Pattern

**문제**: 정산 완료 DB 저장과 Kafka 이벤트 발행이 별개 트랜잭션이면, 둘 중 하나만 성공하는 경우 데이터 불일치 발생

**해결**: 동일 트랜잭션 내 Outbox 테이블에 이벤트를 저장하고, Poller가 비동기로 Kafka에 발행

```
Settlement completePayout()
  └── (same transaction)
      SettlementOutboxEvent 저장
          ↓ (transaction commit)
      SettlementOutboxPoller (5초 주기)
          ↓ SKIP_LOCKED 비관적 락
      kafkaTemplate.send()
          ↓ 성공 시 SENT / 실패 시 재시도 (max 5회)
```

**Outbox Poller 핵심 로직**:

```java
// SKIP_LOCKED: 다른 인스턴스가 처리 중인 row 자동 스킵 → 다중 인스턴스 안전
@Lock(LockModeType.PESSIMISTIC_WRITE)
@QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
List<SettlementOutboxEvent> findPendingEventsWithLock(Pageable pageable);
```

**Outbox 이벤트 상태 전이**:

```
PENDING → PROCESSING → SENT
                    ↘ FAILED (재시도 초과)
```

**설정**:

```yaml
outbox:
  poller:
    interval-ms: 5000      # 5초 주기
    batch-size: 100        # 한 번에 100개
    max-retry: 5           # 최대 5회 재시도
    retention-days: 7      # 7일 후 자동 삭제
```

---

### 3. 3계층 멱등성 설계

배치 재실행 및 동시 실행 시에도 중복 정산이 발생하지 않도록 3개의 독립적인 멱등성 레이어 적용

#### 레이어 1 — CAS 기반 Claim (정산 후보 항목 선점)

`collectedAt IS NULL` 조건부 UPDATE로 원자적 선점. 동시에 여러 배치가 같은 항목을 처리하는 경쟁 조건 방지.

```java
@Modifying
@Query("""
    UPDATE SettlementCandidateItem s
    SET s.collectedAt = :collectedAt
    WHERE s.id = :id
      AND s.collectedAt IS NULL
""")
int markCollectedIfUncollected(Long id, LocalDateTime collectedAt);
```

claim 실패(이미 다른 배치에서 선점) 시 해당 항목 스킵:

```java
boolean claimed = settlementFacade.claimCandidateItem(candidateItem.getId());
if (!claimed) return List.of();  // 중복 처리 방지
```

#### 레이어 2 — DB Unique Constraint (정산 항목 중복 저장 방지)

```java
@Table(
    name = "settlement_item",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "settlement_item_unique",
            columnNames = {"settlement_id", "order_item_id", "event_type"})
    })
public class SettlementItem { ... }
```

#### 레이어 3 — 결정론적 eventId (Outbox 이벤트 중복 방지)

같은 `batchExecutionId`면 항상 같은 `eventId` 생성 → Outbox UNIQUE 제약으로 중복 저장 차단

```java
// "settlement-monthly-completed:{batchExecutionId}"
public static SettlementCompletedPayoutEvent of(
    Long batchExecutionId, List<SettlementCompletedPayoutDto> payouts) {
    return new SettlementCompletedPayoutEvent(
        String.valueOf(batchExecutionId),
        EVENT_ID_PREFIX + batchExecutionId,  // 결정론적 ID
        payouts);
}
```

```java
@Table(
    name = "settlement_outbox_event",
    uniqueConstraints = {@UniqueConstraint(columnNames = "event_id")})
public class SettlementOutboxEvent { ... }
```

---

### 4. Kafka Event-Driven 구현

#### 이벤트 소비

```java
@KafkaListener(topics = "order-events", groupId = "settlement-service")
@Transactional(propagation = REQUIRES_NEW)  // 독립 트랜잭션 보장
public void handleOrderEvent(DomainEventEnvelope envelope, Acknowledgment ack) {
    // OrderPurchaseConfirmedEvent → 정산 후보 항목 수집
    settlementFacade.collectCandidateItems(event.orderDto().getOrderId());
    ackAfterCommit(ack);  // 트랜잭션 커밋 후 Ack → 처리 유실 방지
}

@KafkaListener(topics = "member-events", groupId = "settlement-service")
@Transactional(propagation = REQUIRES_NEW)
public void handleMemberEvent(DomainEventEnvelope envelope, Acknowledgment ack) {
    // MemberSignupEvent → 판매자 정보 동기화
    settlementFacade.syncMember(event.memberId(), event.role());
    ackAfterCommit(ack);
}
```

**핵심 포인트**:
- `REQUIRES_NEW`: 컨슈머 처리마다 독립 트랜잭션
- `ackAfterCommit`: `TransactionSynchronization`으로 커밋 완료 후 Ack 전송 → 트랜잭션 롤백 시 메시지 재처리 보장

---

## 해결한 기술적 문제

| 문제 | 원인 | 해결 |
|------|------|------|
| 중복 정산 | 배치 재실행 또는 동시 실행 | CAS UPDATE + Unique Constraint |
| 배치 실패 후 데이터 불일치 | PROCESSING 상태로 중간 멈춤 | JobExecutionListener 자동 롤백 |
| 이벤트 유실 | 정산 저장 성공 후 이벤트 발행 실패 | Transactional Outbox Pattern |
| 중복 이벤트 발행 | Outbox Poller 재시도 | 결정론적 eventId + Unique 제약 |
| 다중 인스턴스 동시 실행 | 여러 Poller가 같은 이벤트 처리 | SKIP_LOCKED Pessimistic Lock |

---

## 테스트

| 테스트 | 검증 내용 |
|--------|----------|
| `SettlementIdempotencyTest` | 같은 후보 항목 2회 claim 시 첫 번째만 성공, 중복 저장 없음 |
| `SettlementMonthlyJobOutboxIntegrationTest` | 배치 실행 후 Outbox 저장, eventId 멱등키 확인 |
| `SettlementOutboxPollerIntegrationTest` | Poller 실행 후 Kafka 발행, PENDING→PROCESSING→SENT 상태 전이 |

전체 테스트는 `@SpringBootTest` 기반 통합 테스트로 실제 DB/Kafka 환경에서 검증
