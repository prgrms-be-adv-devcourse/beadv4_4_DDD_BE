# 결제 동시성 문제 (Lost Update)

## 개요

여러 사용자가 동시에 결제를 처리할 때 발생하는 동시성 문제(Lost Update)에 대한 설명입니다.

## 문제 상황

- **Holder 계좌**: 1개 (상점 운영자 계좌)
- **Buyer 계좌**: 20개 (구매자 계좌)
- **시나리오**: 20명의 서로 다른 buyer가 동시에 각각 1,000원씩 결제

### 예상 결과
- Holder 잔액 = 0 + (1,000 × 20) = **20,000원**
- 각 Buyer 잔액 = 20,000 - 1,000 = **19,000원**

### 실제 결과 (동시성 문제 발생 시)
- Holder 잔액 = **20,000원보다 적음** (Lost Update 발생)
- 각 Buyer 잔액은 정상적으로 차감됨

## 동시성 문제 발생 원인

### 1. `executeWithoutLock` 메서드의 문제점

```java
@VisibleForTesting
public void executeWithoutLock(PaymentProcessContext paymentProcessContext) {
    // Lock 없이 단순 조회
    PaymentAccount holderAccount = paymentAccountSupport.getHolderAccount();
    // → findByMemberId() 호출 (Lock 없음)
    
    PaymentAccount buyerAccount = 
        paymentAccountSupport.getPaymentAccountByMemberId(paymentProcessContext.buyerId());
    
    processPayment(holderAccount, buyerAccount, paymentProcessContext);
}
```

**문제점**: `findByMemberId()`는 Lock을 사용하지 않아 여러 스레드가 동시에 같은 데이터를 읽을 수 있습니다.

### 2. `PaymentAccount.credit()` 메서드

```java
public void credit(
    BigDecimal amount,
    PaymentEventType paymentEventType,
    Long relId,
    ReferenceType referenceType) {
    validateAmount(amount);
    BigDecimal balanceBefore = this.balance;  // ← 여러 스레드가 동시에 같은 값 읽음
    this.balance = this.balance.add(amount);   // ← 각자 메모리에서 계산
    addPaymentAccountLog(...);
}
```

### 3. 동시 실행 시나리오

```
시간 | Thread 1 (Buyer 1)          | Thread 2 (Buyer 2)          | Thread 3 (Buyer 3)          | DB 실제 값
-----|------------------------------|------------------------------|------------------------------|------------
T1   | holderAccount 조회            | holderAccount 조회           | holderAccount 조회           | balance = 0
     | balance = 0 읽음              | balance = 0 읽음             | balance = 0 읽음             |
T2   | balance = 0 + 1,000 = 1,000  | balance = 0 + 1,000 = 1,000 | balance = 0 + 1,000 = 1,000 | balance = 0
     | (메모리상 계산)                 | (메모리상 계산)                 | (메모리상 계산)                |
T3   | 커밋: balance = 1,000         |                             |                             | balance = 1,000
T4   |                              | 커밋: balance = 1,000        |                             | balance = 1,000 (덮어씀!)
T5   |                              |                             | 커밋: balance = 1,000        | balance = 1,000 (덮어씀!)
```

### 핵심 문제점

1. **읽기 단계**: 여러 스레드가 동시에 같은 balance 값(0)을 읽음
2. **계산 단계**: 각 스레드가 메모리에서 독립적으로 계산 (0 + 1,000 = 1,000)
3. **쓰기 단계**: 마지막 커밋만 반영되어 이전 업데이트가 손실됨 (**Lost Update**)

## 해결 방법: Pessimistic Lock 사용

### `execute` 메서드 (Lock 사용)

```java
public void execute(PaymentProcessContext paymentProcessContext) {
    // 1. 결제 계좌에 대한 Lock 획득
    LockedPaymentAccounts accounts =
        paymentAccountLockManager.getEntitiesForUpdateInOrder(
            paymentAccountConfig.getHolderMemberId(), 
            paymentProcessContext.buyerId());
    
    // 2. 결제 계좌 영속성 획득 (Lock이 걸린 상태)
    PaymentAccount holderAccount = accounts.get(paymentAccountConfig.getHolderMemberId());
    PaymentAccount buyerAccount = accounts.get(paymentProcessContext.buyerId());
    
    // 3. 결제 처리
    processPayment(holderAccount, buyerAccount, paymentProcessContext);
}
```

### `PaymentAccountRepository`의 Lock 메서드

```java
// Lock 없이 조회 (동시성 문제 발생 가능)
Optional<PaymentAccount> findByMemberId(Long memberId);

// Pessimistic Write Lock 사용 (동시성 문제 해결)
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT pa FROM PaymentAccount pa WHERE pa.member.id = :memberId")
Optional<PaymentAccount> findByMemberIdWithLock(@Param("memberId") Long memberId);
```

### PESSIMISTIC_WRITE Lock의 효과

- 한 스레드가 조회하면 다른 스레드는 **대기**
- 순차적으로 처리되어 모든 입금이 **누적**됨
- 최종 잔액 = 0 + 1,000 + 1,000 + ... + 1,000 = **20,000원** ✅

### Lock 사용 시 실행 시나리오

```
시간 | Thread 1 (Buyer 1)          | Thread 2 (Buyer 2)          | Thread 3 (Buyer 3)          | DB 실제 값
-----|------------------------------|------------------------------|------------------------------|------------
T1   | Lock 획득                    | 대기...                     | 대기...                     | balance = 0
     | balance = 0 읽음             |                              |                              |
T2   | balance = 0 + 1,000 = 1,000 |                              |                              | balance = 0
     | 커밋: balance = 1,000        |                              |                              |
T3   | Lock 해제                    | Lock 획득                    | 대기...                     | balance = 1,000
     |                              | balance = 1,000 읽음         |                              |
T4   |                              | balance = 1,000 + 1,000      |                              | balance = 1,000
     |                              | 커밋: balance = 2,000         |                              |
T5   |                              | Lock 해제                    | Lock 획득                    | balance = 2,000
     |                              |                              | balance = 2,000 읽음         |
T6   |                              |                              | balance = 2,000 + 1,000      | balance = 2,000
     |                              |                              | 커밋: balance = 3,000         |
```

## 테스트 코드

### 동시성 문제 검증 테스트

```java
@Test
@DisplayName("동시 결제 처리 테스트 - 동시성 문제로 인한 잔액 불일치 검증")
void testConcurrentPaymentProcessingWithoutLock() throws InterruptedException {
    // given
    // 20명의 buyer가 동시에 결제
    
    // when
    // executeWithoutLock() 사용 (Lock 없음)
    
    // then
    // Holder 잔액이 예상값과 일치하지 않아야 함 (동시성 문제 발생)
    assertThat(holderAccount.getBalance())
        .isNotEqualByComparingTo(expectedHolderBalance);
}
```

### Lock 적용 검증 테스트

```java
@Test
@DisplayName("동시 결제 처리 테스트 - 락 적용으로 잔액 일치 검증")
void testConcurrentPaymentProcessingWithLock() throws InterruptedException {
    // given
    // 20명의 buyer가 동시에 결제
    
    // when
    // execute() 사용 (Lock 사용)
    
    // then
    // Holder 잔액이 예상값과 일치해야 함 (동시성 문제 해결)
    assertThat(holderAccount.getBalance())
        .isEqualByComparingTo(expectedHolderBalance);
}
```

## 정리

### Lost Update 문제

- **정의**: 여러 트랜잭션이 동시에 같은 데이터를 읽고 수정할 때, 마지막 커밋만 반영되어 이전 업데이트가 손실되는 문제
- **발생 조건**: 
  - Lock 없이 데이터를 읽고 수정
  - 여러 트랜잭션이 동시에 실행
- **해결 방법**: Pessimistic Lock (PESSIMISTIC_WRITE) 사용

### 결제 시스템에서의 중요성

- **금액 정확성**: 잔액 계산 오류는 심각한 문제
- **데이터 무결성**: 모든 거래가 정확히 기록되어야 함
- **동시성 제어**: Lock을 통한 순차 처리로 데이터 일관성 보장

## 참고 자료

- [PaymentProcessConcurrencyTest.java](../../src/test/java/com/modeunsa/boundedcontext/payment/app/usecase/PaymentProcessConcurrencyTest.java)
- [PaymentProcessUseCase.java](../../src/main/java/com/modeunsa/boundedcontext/payment/app/usecase/PaymentProcessUseCase.java)
- [PaymentAccountRepository.java](../../src/main/java/com/modeunsa/boundedcontext/payment/out/PaymentAccountRepository.java)
