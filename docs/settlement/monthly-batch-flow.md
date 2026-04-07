# 월 정산 배치 흐름 (monthlySettlementJob)

---

## 한눈에 보는 흐름

| 단계 | 클래스 | 메서드 / Bean | 동작 | 상태 변화 |
|------|--------|---------------|------|-----------|
| 1. 스케줄러 | `SettlementScheduler` | `runOnThe25th()` | 매월 25일 04:00 KST 트리거 | — |
| 2. Job 실행 | `SettlementJobLauncher` | `runMonthlyPayoutJob()` | JobParameters 생성 → `jobOperator.start()` | — |
| 3. Step 1 | `SettlementMonthSettlementStepConfig` | `reserveMonthlySettlementStep` | 전달 PENDING 정산 선점 | `PENDING → PROCESSING` |
| 4. Step 2 | `SettlementMonthSettlementStepConfig` | `completeMonthlySettlementStep` | 완료 처리 + 이벤트 발행 | `PROCESSING → COMPLETED` |
| 5. Outbox 저장 | `SettlementOutboxPublisher` | `publish()` | 동일 트랜잭션 내 Outbox 저장 | `PENDING` |
| 6. Outbox Polling | `SettlementOutboxPoller` | `poll()` | 5초 주기로 Kafka 발행 | `PENDING → SENT / FAILED` |
| 실패 시 | `SettlementMonthlyJobExecutionListener` | `afterJob()` | PROCESSING 전체 롤백 | `PROCESSING → PENDING` |

---

## 1. 스케줄러 트리거 — `SettlementScheduler`

매월 25일 04:00 KST에 자동 실행

```
@Scheduled(cron = "${settlement.scheduler.monthly-25-04:}", zone = "Asia/Seoul")
runOnThe25th()
  → settlementJobLauncher.runMonthlyPayoutJob()
```

---

## 2. JobParameters 생성 및 Job 실행 — `SettlementJobLauncher`

전달(now - 1개월)을 기준으로 파라미터를 구성하고 Job을 실행

```
runMonthlyPayoutJob()
  → targetMonth = LocalDate.now().minusMonths(1)
  → JobParameters: runDateTime, settlementYear, settlementMonth, settlementPeriod
  → jobOperator.start(monthlySettlementJob, jobParameters)
```

---

## 3. Job 구성 — `SettlementJobConfig`

2개의 Step을 순차 실행, 실패 감지용 Listener 등록

```
monthlySettlementJob
  .listener(settlementMonthlyJobExecutionListener)
  .start(reserveMonthlySettlementStep)
  .next(completeMonthlySettlementStep)
```

---

## 4. Step 1 — `reserveMonthlySettlementStep` (Tasklet)

PENDING 상태 정산을 선점하여 PROCESSING으로 전환

```
batchExecutionId = jobExecution.getId()
→ findBySettlementYearAndSettlementMonthAndStatus(year, month, PENDING)
→ settlement.markProcessing(batchExecutionId)   // PENDING → PROCESSING
```

---

## 5. Step 2 — `completeMonthlySettlementStep` (Tasklet)

정산을 완료 처리하고 이벤트를 발행

```
→ findByBatchExecutionIdAndStatus(batchExecutionId, PROCESSING)
→ settlement.completePayout()   // PROCESSING → COMPLETED, payoutAt = now
→ eventPublisher.publish(SettlementCompletedPayoutEvent.of(batchExecutionId, payouts))
    └── SettlementOutboxPublisher: outbox 테이블에 PENDING으로 저장 (same transaction)
```

---

## 7. 실패 시 — `SettlementMonthlyJobExecutionListener.afterJob()`

Job이 실패하면 PROCESSING 상태를 PENDING으로 롤백하여 재실행 가능 상태로 복구

```
isUnsuccessful() == true
→ findByBatchExecutionIdAndStatus(batchExecutionId, PROCESSING)
→ settlement.rollbackToPending()   // PROCESSING → PENDING
```

---

## 8. Outbox Polling — `SettlementOutboxPoller`

5초 주기로 PENDING 이벤트를 Kafka로 발행

```
5초 주기 스케줄
→ findPendingEventsWithLock (SKIP_LOCKED)
→ kafkaTemplate.send(topic: "settlement-events")
→ 성공: SENT / 실패: 재시도 (max 5회) → FAILED
```
