package com.modeunsa.boundedcontext.payment.app.usecase;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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
            PaymentMember.create(2L, "hodler@example.com", "Holder", MemberStatus.ACTIVE));
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
  @DisplayName("동시성 테스트")
  void testConcurrentPaymentProcessing() throws InterruptedException {

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
              e.printStackTrace();
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

    // 이론상 기대값
    BigDecimal expectedBuyer =
        BigDecimal.valueOf(20_000).subtract(amount.multiply(BigDecimal.valueOf(threadCount)));
    BigDecimal expectedHolder = amount.multiply(BigDecimal.valueOf(threadCount));

    System.out.println(
        "buyer="
            + buyerAccount.getBalance()
            + ", holder="
            + holderAccount.getBalance()
            + ", expectedBuyer="
            + expectedBuyer
            + ", expectedHolder="
            + expectedHolder);

    assertThat(buyerAccount.getBalance()).isNotEqualTo(expectedBuyer);
    assertThat(holderAccount.getBalance()).isNotEqualTo(expectedHolder);
  }
}
