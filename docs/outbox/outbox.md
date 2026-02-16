## 개요
Outbox 패턴의 핵심 데이터 모델 구현

## OutboxStatus
```java
public enum OutboxStatus {
    PENDING,      // 대기: 생성됨, 발송 전
    PROCESSING,   // 처리중: 발송 시도 중
    SENT,         // 성공: 발송 완료
    FAILED        // 실패: 발송 실패
}
```

PENDING → PROCESSING → SENT
↘ FAILED


```text
## OutboxEvent 엔티티

필드              | 타입            | 설명
------------------------------------------------------
id               | Long           | PK
aggregateType    | String         | 집합체 타입 (Order, Member 등)
aggregateId      | String         | 집합체 ID
eventType        | String         | 이벤트 클래스명
topic            | String         | Kafka 토픽
payload          | LONGTEXT       | JSON 페이로드
status           | OutboxStatus   | PENDING/PROCESSING/SENT/FAILED
createdAt        | LocalDateTime  | 생성 시각
sentAt           | LocalDateTime  | 발송 완료 시각
retryCount       | int            | 재시도 횟수
lastErrorMessage | String         | 마지막 에러 메시지
version          | Long           | 낙관적 락
```

### 인덱스 전략
- `idx_outbox_status_created`: 폴링 시 PENDING 상태 조회 최적화
- `idx_outbox_aggregate`: 특정 Aggregate 이벤트 조회

<br />

### OutboxPublisher

## 개요
이벤트를 Outbox 테이블에 저장하는 컴포넌트

## 핵심 로직
```java
@Transactional(propagation = Propagation.MANDATORY)
public void saveToOutbox(Object event) {
    // 1. 이벤트에서 메타데이터 추출 (aggregateType, aggregateId, topic)
    // 2. JSON 직렬화
    // 3. OutboxEvent 생성 및 저장
}
```

### Propagation.MANDATORY
- 기존 트랜잭션이 **반드시** 존재해야 함
- 트랜잭션 없이 호출 시 예외 발생
- 도메인 로직과 Outbox 저장이 같은 트랜잭션에서 실행됨을 보장

<br />

### OutboxPoller

## 개요
주기적으로 Outbox 테이블을 폴링하여 Kafka로 발행하는 스케줄러

## 핵심 로직
```java
@Scheduled(fixedDelayString = "${outbox.poller.interval-ms:5000}")
@Transactional
public void pollAndPublish() {
    // 1. PENDING 상태 이벤트 조회 (배치 크기만큼)
    // 2. 각 이벤트에 대해:
    //    - PROCESSING으로 상태 변경
    //    - Kafka로 발행
    //    - 성공 시 SENT, 실패 시 PENDING (재시도) 또는 FAILED
}
```

## 설정
```yaml
outbox:
  enabled: true            # Outbox 활성화 여부
  timeoutMs: 10000         # 발행 타임아웃 (10초)
  poller:
    enabled: true          # 폴러 활성화
    interval-ms: 5000      # 폴링 주기 (5초)
    batch-size: 100        # 배치 크기
    max-retry: 5           # 최대 재시도 횟수
    retention-days: 7         # 보관 기간 (7일)
  cleanup:
    cron: "0 0 3 * * *"    # 정리 스케줄 (매일 새벽 3시)
```

## 재시도 전략
- 발행 실패 시 `retryCount` 증가
- `retryCount < maxRetry`: PENDING으로 복귀 (다음 폴링에서 재시도)
- `retryCount >= maxRetry`: FAILED로 변경 (수동 처리 필요)

## 정리 스케줄
- 매일 새벽 3시 실행
- 7일 이상 지난 SENT 이벤트 삭제

---

## global outbox 패키지 클래스 (common)

`com.modeunsa.global.kafka.outbox` 패키지에 정의된 공통 인터페이스/구현체. 각 바운디드 컨텍스트(payment 등)는 이 인터페이스를 구현해 Outbox를 사용한다.

### OutboxStatus (enum)

아웃박스 이벤트의 생명주기 상태.

| 값 | 설명 |
|----|------|
| PENDING | 대기: 생성됨, 발송 전 |
| PROCESSING | 처리 중: 폴러가 선점해 발송 시도 중 |
| SENT | 발송 완료 |
| FAILED | 발송 실패 (maxRetry 초과 등) |

---

### OutboxEventView (interface)

폴링·발행 시 필요한 아웃박스 행의 읽기 전용 뷰. 엔티티/프로젝션 등이 이 인터페이스를 구현한다.

| 메서드 | 용도 |
|--------|------|
| getId() | PK. 상태 변경·삭제 시 사용 |
| getTopic(), getPayload(), getAggregateId(), getEventType() | Kafka 발행 시 사용 |
| getEventId() | 저장 시점 이벤트 ID. 멱등·재처리 추적용 |
| getTraceId() | 분산 추적용  |
| getStatus() | OutboxStatus |

---

### OutboxPublisher (interface)

이벤트를 Outbox 테이블에 넣는 진입점. **도메인 서비스/유스케이스에서 호출**하며, 구현체는 `Propagation.MANDATORY` 등으로 기존 트랜잭션 안에서 저장한다.

```java
void saveToOutbox(Object event);
```

- 구현: 각 서비스의 `XxxOutboxPublisher`가 메타데이터 추출·JSON 직렬화·Outbox 엔티티 저장을 수행.

---

### OutboxReader (interface)

아웃박스 **조회** 전용. 폴러가 PENDING 조회·정리 대상 ID 조회 시 사용한다.

| 메서드 | 설명 |
|--------|------|
| findPendingEventsWithLock(Pageable) | PENDING 이벤트를 **FOR UPDATE SKIP LOCKED** 등으로 조회. 폴링 시 사용 |
| findDeleteTargetIds(before, Pageable) | `before` 이전에 SENT된 이벤트 ID 목록. 정리(cleanup) 시 사용 |

구현: 각 서비스의 `XxxOutboxQueryRepository` 등이 Querydsl/JPA로 구현.

---

### OutboxStore (interface)

아웃박스 **상태 변경·삭제** 전용. 폴러가 조회한 이벤트를 PROCESSING → SENT/FAILED 로 바꾸거나, 정리 시 삭제할 때 사용한다.

| 메서드 | 설명 |
|--------|------|
| markProcessing(id) | PENDING → PROCESSING (선점 완료) |
| markSent(id) | PROCESSING → SENT, sentAt 설정 |
| markFailed(id, errorMessage, maxRetry) | PROCESSING → FAILED 또는 PENDING(재시도). retryCount 반영 |
| deleteAlreadySentEventByIds(ids) | SENT 상태 행 삭제 (정리 스케줄) |

구현: 각 서비스의 Outbox 저장소/서비스 레이어에서 엔티티 조회 후 업데이트·삭제.

---

### OutboxPollerRunner (class, @Component)

폴링·정리 **실행 흐름**만 담당하는 공통 러너. Reader/Store는 인터페이스로 주입받아 서비스별 구현을 사용한다.

**역할**

1. **runPolling(reader, store, batchSize, maxRetry, timeoutSeconds)**  
   - `self.findPendingEvents(...)` 로 **새 트랜잭션(REQUIRES_NEW)** 안에서 PENDING 조회 + `markProcessing` 실행 (프록시 경유로 트랜잭션 적용).  
   - 조회된 이벤트마다 Kafka 발행 후 성공 시 `markSent`, 실패 시 `markFailed`.  
   - 발행은 `kafkaTemplate.send(...).get(timeoutSeconds)` 로 동기 대기.

2. **runCleanup(reader, store, before, batchSize)**  
   - `reader.findDeleteTargetIds(before, ...)` 로 삭제 대상 ID 조회 후 `store.deleteAlreadySentEventByIds(ids)` 호출.
