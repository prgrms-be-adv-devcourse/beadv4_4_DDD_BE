package com.modeunsa.boundedcontext.payment.app.usecase;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentProcessContext;
import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.domain.types.MemberStatus;
import com.modeunsa.boundedcontext.payment.out.PaymentAccountLogRepository;
import com.modeunsa.boundedcontext.payment.out.PaymentAccountRepository;
import com.modeunsa.boundedcontext.payment.out.PaymentMemberRepository;
import com.modeunsa.boundedcontext.payment.out.PaymentRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

  private Long holderId;
  private Long buyerId;
  private String orderNo;
  private Long orderId;

  @BeforeEach
  void setUp() {

    paymentAccountLogRepository.deleteAll();
    paymentAccountRepository.deleteAll();
    paymentMemberRepository.deleteAll();
    paymentRepository.deleteAll();

    PaymentMember holderMember =
        paymentMemberRepository.save(
            PaymentMember.create(2L, "holder@example.com", "Holder", MemberStatus.ACTIVE));
    PaymentMember buyerMember =
        paymentMemberRepository.save(
            PaymentMember.create(1000L, "user1@example.com", "구매자", MemberStatus.ACTIVE));

    paymentAccountRepository.save(PaymentAccount.create(holderMember));

    PaymentAccount buyerAccount =
        PaymentAccount.builder().member(buyerMember).balance(BigDecimal.valueOf(20_000)).build();

    paymentAccountRepository.save(buyerAccount);

    this.orderNo = "ORDER12345";
    this.orderId = 1L;
    this.holderId = holderMember.getId();
    this.buyerId = buyerMember.getId();

    PaymentId paymentId = PaymentId.create(this.buyerId, orderNo);

    Payment payment =
        Payment.create(
            paymentId, orderId, BigDecimal.valueOf(20_000), LocalDateTime.now().plusDays(1));

    paymentRepository.save(payment);
  }

  @Test
  @DisplayName("동시 결제 처리 테스트 - 동시성 문제로 인한 잔액 불일치 검증")
  void testConcurrentPaymentProcessingWithoutLock() throws InterruptedException {

    // given
    BigDecimal amount = BigDecimal.valueOf(1_000);
    var threadCount = 20;

    final PaymentProcessContext paymentProcessContext =
        PaymentProcessContext.builder()
            .buyerId(buyerId)
            .orderNo(orderNo)
            .orderId(orderId)
            .needsCharge(false)
            .chargeAmount(BigDecimal.ZERO)
            .totalAmount(amount)
            .build();

    ExecutorService executor = Executors.newFixedThreadPool(10);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(threadCount);

    // when
    for (int i = 0; i < threadCount; i++) {
      executor.submit(
          () -> {
            try {
              // 모든 준비될 때까지 대기
              startLatch.await();

              paymentProcessUseCase.executeWithoutLock(paymentProcessContext);
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

    // then
    PaymentAccount holderAccount = paymentAccountRepository.findByMemberId(holderId).orElseThrow();
    PaymentAccount buyerAccount = paymentAccountRepository.findByMemberId(buyerId).orElseThrow();

    BigDecimal expectedBuyer =
        BigDecimal.valueOf(20_000).subtract(amount.multiply(BigDecimal.valueOf(threadCount)));
    BigDecimal expectedHolder = amount.multiply(BigDecimal.valueOf(threadCount));

    logExpectedAndActualBalances(
        buyerAccount.getBalance(), holderAccount.getBalance(), expectedBuyer, expectedHolder);

    assertThat(buyerAccount.getBalance()).isNotEqualByComparingTo(expectedBuyer);
    assertThat(holderAccount.getBalance()).isNotEqualByComparingTo(expectedHolder);
  }

  @Test
  @DisplayName("동시 결제 처리 테스트 - 락 적용으로 잔액 일치 검증")
  void testConcurrentPaymentProcessingWithLock() throws InterruptedException {

    // given
    BigDecimal amount = BigDecimal.valueOf(1_000);
    var threadCount = 20;

    final PaymentProcessContext paymentProcessContext =
        PaymentProcessContext.builder()
            .buyerId(buyerId)
            .orderNo(orderNo)
            .orderId(orderId)
            .needsCharge(false)
            .chargeAmount(BigDecimal.ZERO)
            .totalAmount(amount)
            .build();

    ExecutorService executor = Executors.newFixedThreadPool(10);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(threadCount);

    // when
    for (int i = 0; i < threadCount; i++) {
      executor.submit(
          () -> {
            try {
              // 모든 준비될 때까지 대기
              startLatch.await();

              paymentProcessUseCase.execute(paymentProcessContext);
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

    // then
    PaymentAccount holderAccount = paymentAccountRepository.findByMemberId(holderId).orElseThrow();
    PaymentAccount buyerAccount = paymentAccountRepository.findByMemberId(buyerId).orElseThrow();

    BigDecimal expectedBuyer =
        BigDecimal.valueOf(20_000).subtract(amount.multiply(BigDecimal.valueOf(threadCount)));
    BigDecimal expectedHolder = amount.multiply(BigDecimal.valueOf(threadCount));

    logExpectedAndActualBalances(
        buyerAccount.getBalance(), holderAccount.getBalance(), expectedBuyer, expectedHolder);

    assertThat(buyerAccount.getBalance()).isEqualByComparingTo(expectedBuyer);
    assertThat(holderAccount.getBalance()).isEqualByComparingTo(expectedHolder);
  }

  private void logExpectedAndActualBalances(
      BigDecimal buyerBalance,
      BigDecimal holderBalance,
      BigDecimal expectedBuyer,
      BigDecimal expectedHolder) {
    log.info(
        "buyer={}, holder={}, expectedBuyer={}, expectedHolder={}",
        buyerBalance,
        holderBalance,
        expectedBuyer,
        expectedHolder);
  }
}
