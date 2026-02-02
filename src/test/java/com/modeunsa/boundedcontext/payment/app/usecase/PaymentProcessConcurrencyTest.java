package com.modeunsa.boundedcontext.payment.app.usecase;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentProcessContext;
import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.domain.types.MemberStatus;
import com.modeunsa.boundedcontext.payment.domain.types.ProviderType;
import com.modeunsa.boundedcontext.payment.out.PaymentAccountLogRepository;
import com.modeunsa.boundedcontext.payment.out.PaymentAccountRepository;
import com.modeunsa.boundedcontext.payment.out.PaymentMemberRepository;
import com.modeunsa.boundedcontext.payment.out.PaymentRepository;
import com.modeunsa.global.config.PaymentAccountConfig;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@Tag("ignore")
@SpringBootTest
@ActiveProfiles("test")
class PaymentProcessConcurrencyTest {

  @Autowired private PaymentProcessUseCase paymentProcessUseCase;

  @Autowired private PaymentRepository paymentRepository;

  @Autowired private PaymentMemberRepository paymentMemberRepository;

  @Autowired private PaymentAccountRepository paymentAccountRepository;

  @Autowired private PaymentAccountLogRepository paymentAccountLogRepository;

  @Autowired private PaymentAccountConfig paymentAccountConfig;

  private Long holderId;
  private List<Long> buyerIds;
  private BigDecimal amount;
  private int threadCount;
  private List<PaymentProcessContext> contexts;

  @BeforeEach
  void setUp() {

    paymentAccountLogRepository.deleteAll();
    paymentAccountRepository.deleteAll();
    paymentMemberRepository.deleteAll();
    paymentRepository.deleteAll();

    PaymentMember holderMember =
        paymentMemberRepository.save(
            PaymentMember.create(
                paymentAccountConfig.getHolderMemberId(),
                "holder@example.com",
                "Holder",
                MemberStatus.ACTIVE));
    paymentAccountRepository.save(PaymentAccount.create(holderMember));
    this.holderId = holderMember.getId();

    this.threadCount = 20;
    this.amount = BigDecimal.valueOf(1_000);
    this.buyerIds = new ArrayList<>();
    this.contexts = new ArrayList<>();

    for (int i = 0; i < threadCount; i++) {
      // buyer 생성
      PaymentMember buyerMember =
          paymentMemberRepository.save(
              PaymentMember.create(
                  1000L + i, "buyer" + i + "@example.com", "구매자" + i, MemberStatus.ACTIVE));

      // buyer 계정 생성 (잔액 20_000)
      PaymentAccount buyerAccount =
          PaymentAccount.builder().member(buyerMember).balance(BigDecimal.valueOf(20_000)).build();
      paymentAccountRepository.save(buyerAccount);

      Long buyerId = buyerMember.getId();
      buyerIds.add(buyerId);

      // 각 buyer의 주문 정보
      String orderNo = "ORDER" + i;
      Long orderId = (long) (i + 1);

      // Payment 생성
      PaymentId paymentId = PaymentId.create(buyerId, orderNo);
      Payment payment =
          Payment.create(
              paymentId,
              orderId,
              BigDecimal.valueOf(20_000),
              LocalDateTime.now().plusDays(1),
              ProviderType.MODEUNSA_PAY);
      paymentRepository.save(payment);

      // PaymentProcessContext 생성
      PaymentProcessContext ctx =
          PaymentProcessContext.builder()
              .buyerId(buyerId)
              .orderNo(orderNo)
              .orderId(orderId)
              .needsCharge(false)
              .chargeAmount(BigDecimal.ZERO)
              .totalAmount(amount)
              .build();

      contexts.add(ctx);
    }
  }

  @Test
  @DisplayName("동시 결제 처리 테스트 - 동시성 문제로 인한 잔액 불일치 검증")
  void testConcurrentPaymentProcessingWithoutLock() throws InterruptedException {

    // given
    ExecutorService executor = Executors.newFixedThreadPool(10);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(threadCount);

    // when
    for (int i = 0; i < threadCount; i++) {
      final PaymentProcessContext ctx = contexts.get(i);

      executor.submit(
          () -> {
            try {
              // 모든 준비될 때까지 대기
              startLatch.await();

              paymentProcessUseCase.executeWithoutLock(ctx);
            } catch (Exception e) {
              fail("동시성 작업 중 예외 발생", e);
            } finally {
              doneLatch.countDown();
            }
          });
    }

    startLatch.countDown();
    doneLatch.await();
    executor.shutdown();
    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
      executor.shutdownNow();
    }

    // then
    PaymentAccount holderAccount = paymentAccountRepository.findByMemberId(holderId).orElseThrow();

    // 각 buyer의 잔액 확인 (각 buyer는 20_000 - 1_000 = 19_000이 되어야 함)
    BigDecimal expectedBuyerBalance = BigDecimal.valueOf(20_000).subtract(amount);
    // holder는 모든 buyer들의 결제 금액 합계를 받아야 함 (1_000 * 20 = 20_000)
    BigDecimal expectedHolderBalance = amount.multiply(BigDecimal.valueOf(threadCount));

    // 모든 buyer들의 잔액 확인
    // buyer 는 각각의 계좌에 하나씩 접근한 것으로 동시성 문제 없음
    for (Long buyerId : buyerIds) {
      PaymentAccount buyerAccount = paymentAccountRepository.findByMemberId(buyerId).orElseThrow();
      log.info(
          "Buyer {} balance: {}, expected: {}",
          buyerId,
          buyerAccount.getBalance(),
          expectedBuyerBalance);
    }

    // Holder 는 동시에 여러 트랜잭션이 하나의 Row 에 접근한 것으로 동시성 문제 발생
    log.info("Holder balance: {}, expected: {}", holderAccount.getBalance(), expectedHolderBalance);

    // 동시성 문제로 인해 잔액이 일치하지 않아야 함
    assertThat(holderAccount.getBalance()).isNotEqualByComparingTo(expectedHolderBalance);
  }

  @Test
  @DisplayName("동시 결제 처리 테스트 - 락 적용으로 잔액 일치 검증")
  void testConcurrentPaymentProcessingWithLock() throws InterruptedException {

    // given

    ExecutorService executor = Executors.newFixedThreadPool(10);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(threadCount);

    // when
    for (int i = 0; i < threadCount; i++) {
      final PaymentProcessContext ctx = contexts.get(i);

      executor.submit(
          () -> {
            try {
              // 모든 준비될 때까지 대기
              startLatch.await();

              paymentProcessUseCase.execute(ctx);
            } catch (Exception e) {
              fail("동시성 작업 중 예외 발생", e);
            } finally {
              doneLatch.countDown();
            }
          });
    }

    startLatch.countDown();
    doneLatch.await();
    executor.shutdown();
    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
      executor.shutdownNow();
    }

    // then
    PaymentAccount holderAccount = paymentAccountRepository.findByMemberId(holderId).orElseThrow();

    // 각 buyer의 잔액 확인 (각 buyer는 20_000 - 1_000 = 19_000이 되어야 함)
    BigDecimal expectedBuyerBalance = BigDecimal.valueOf(20_000).subtract(amount);
    // holder는 모든 buyer들의 결제 금액 합계를 받아야 함 (1_000 * 20 = 20_000)
    BigDecimal expectedHolderBalance = amount.multiply(BigDecimal.valueOf(threadCount));

    // 모든 buyer들의 잔액 확인
    for (Long buyerId : buyerIds) {
      PaymentAccount buyerAccount = paymentAccountRepository.findByMemberId(buyerId).orElseThrow();
      assertThat(buyerAccount.getBalance()).isEqualByComparingTo(expectedBuyerBalance);
    }

    // 동시에 여러 트랜잭션이 하나의 Row 에 접근했지만 락 적용으로 인해 잔액이 일치해야 함
    log.info("Holder balance: {}, expected: {}", holderAccount.getBalance(), expectedHolderBalance);

    assertThat(holderAccount.getBalance()).isEqualByComparingTo(expectedHolderBalance);
  }
}
