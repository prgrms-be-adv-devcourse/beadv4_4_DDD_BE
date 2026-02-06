package com.modeunsa.boundedcontext.payment.app.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.modeunsa.boundedcontext.payment.app.dto.settlement.PaymentPayoutInfo;
import com.modeunsa.boundedcontext.payment.app.lock.PaymentAccountLockManager;
import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.app.usecase.process.PaymentPayoutCompleteUseCase;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.domain.types.MemberStatus;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.boundedcontext.payment.domain.types.PayoutEventType;
import com.modeunsa.global.config.PaymentAccountConfig;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentPayoutCompleteUseCase 테스트")
class PaymentPayoutCompleteUseCaseTest {

  @Mock private PaymentAccountSupport paymentAccountSupport;
  @Mock private PaymentAccountConfig paymentAccountConfig;

  private PaymentPayoutCompleteUseCase paymentPayoutCompleteUseCase;

  private PaymentMember holderMember;
  private PaymentMember payeeMember;
  private PaymentMember systemMember;
  private PaymentAccount holderAccount;
  private PaymentAccount payeeAccount;
  private PaymentAccount systemAccount;

  @BeforeEach
  void setUp() {
    systemMember = PaymentMember.create(1L, "system@example.com", "시스템", MemberStatus.ACTIVE);
    holderMember = PaymentMember.create(2L, "holder@example.com", "홀더", MemberStatus.ACTIVE);
    payeeMember = PaymentMember.create(1000L, "user1@example.com", "수령인", MemberStatus.ACTIVE);

    holderAccount = PaymentAccount.create(holderMember);
    holderAccount.credit(BigDecimal.valueOf(100000), PaymentEventType.CHARGE_BANK_TRANSFER);

    payeeAccount = PaymentAccount.create(payeeMember);
    systemAccount = PaymentAccount.create(systemMember);

    // 실제 유즈케이스는 PaymentAccountLockManager와 PaymentAccountConfig를 의존성으로 사용하므로
    // PaymentAccountSupport를 주입한 실제 LockManager 인스턴스를 구성한다.
    PaymentAccountLockManager lockManager = new PaymentAccountLockManager(paymentAccountSupport);
    paymentPayoutCompleteUseCase =
        new PaymentPayoutCompleteUseCase(lockManager, paymentAccountConfig);

    when(paymentAccountConfig.getHolderMemberId()).thenReturn(holderMember.getId());
  }

  @Test
  @DisplayName("정산 처리 성공 - FEE 타입: holder에서 차감, system 계좌로 입금")
  void executeSuccessFeeType() {
    // given
    BigDecimal payoutAmount = BigDecimal.valueOf(5000);
    PaymentPayoutInfo payoutInfo =
        new PaymentPayoutInfo(
            1L, systemMember.getId(), payoutAmount, PayoutEventType.FEE, LocalDateTime.now());

    // 락 매니저 내부에서 호출되는 support 메서드 mock
    when(paymentAccountSupport.getPaymentAccountByMemberIdForUpdate(holderMember.getId()))
        .thenReturn(holderAccount);
    when(paymentAccountSupport.getPaymentAccountByMemberIdForUpdate(systemMember.getId()))
        .thenReturn(systemAccount);

    BigDecimal holderBalanceBefore = holderAccount.getBalance();
    BigDecimal systemBalanceBefore = systemAccount.getBalance();

    // when
    paymentPayoutCompleteUseCase.execute(List.of(payoutInfo));

    // then
    assertThat(holderAccount.getBalance())
        .isEqualByComparingTo(holderBalanceBefore.subtract(payoutAmount));
    assertThat(systemAccount.getBalance())
        .isEqualByComparingTo(systemBalanceBefore.add(payoutAmount));
  }

  @Test
  @DisplayName("정산 처리 성공 - AMOUNT 타입: holder에서 차감, payee 계좌로 입금")
  void executeSuccessAmountType() {
    // given
    BigDecimal payoutAmount = BigDecimal.valueOf(10000);
    PaymentPayoutInfo payoutInfo =
        new PaymentPayoutInfo(
            1L, payeeMember.getId(), payoutAmount, PayoutEventType.AMOUNT, LocalDateTime.now());

    when(paymentAccountSupport.getPaymentAccountByMemberIdForUpdate(holderMember.getId()))
        .thenReturn(holderAccount);
    when(paymentAccountSupport.getPaymentAccountByMemberIdForUpdate(payeeMember.getId()))
        .thenReturn(payeeAccount);

    BigDecimal holderBalanceBefore = holderAccount.getBalance();
    BigDecimal payeeBalanceBefore = payeeAccount.getBalance();

    // when
    paymentPayoutCompleteUseCase.execute(List.of(payoutInfo));

    // then
    assertThat(holderAccount.getBalance())
        .isEqualByComparingTo(holderBalanceBefore.subtract(payoutAmount));
    assertThat(payeeAccount.getBalance())
        .isEqualByComparingTo(payeeBalanceBefore.add(payoutAmount));
  }

  @Test
  @DisplayName("정산 처리 - AMOUNT 타입일 때 올바른 PaymentEventType으로 변환되는지 확인")
  void executeVerifyEventTypeConversionAmount() {
    // given
    PaymentPayoutInfo payoutInfo =
        new PaymentPayoutInfo(
            1L,
            payeeMember.getId(),
            BigDecimal.valueOf(10000),
            PayoutEventType.AMOUNT,
            LocalDateTime.now());

    when(paymentAccountSupport.getPaymentAccountByMemberIdForUpdate(holderMember.getId()))
        .thenReturn(holderAccount);
    when(paymentAccountSupport.getPaymentAccountByMemberIdForUpdate(payeeMember.getId()))
        .thenReturn(payeeAccount);

    // when
    paymentPayoutCompleteUseCase.execute(List.of(payoutInfo));

    // then
    assertThat(holderAccount.getPaymentAccountLogs())
        .anyMatch(
            log -> log.getEventType() == PaymentEventType.SETTLEMENT_PAY_PRODUCT_SALES_AMOUNT);
    assertThat(payeeAccount.getPaymentAccountLogs())
        .anyMatch(
            log -> log.getEventType() == PaymentEventType.SETTLEMENT_PAY_PRODUCT_SALES_AMOUNT);
  }

  @Test
  @DisplayName("정산 처리 - FEE 타입일 때 올바른 PaymentEventType으로 변환되는지 확인")
  void executeVerifyEventTypeConversionFee() {
    // given
    PaymentPayoutInfo payoutInfo =
        new PaymentPayoutInfo(
            1L,
            payeeMember.getId(),
            BigDecimal.valueOf(2000),
            PayoutEventType.FEE,
            LocalDateTime.now());

    when(paymentAccountSupport.getPaymentAccountByMemberIdForUpdate(holderMember.getId()))
        .thenReturn(holderAccount);
    when(paymentAccountSupport.getPaymentAccountByMemberIdForUpdate(payeeMember.getId()))
        .thenReturn(payeeAccount);

    // when
    paymentPayoutCompleteUseCase.execute(List.of(payoutInfo));

    // then
    assertThat(holderAccount.getPaymentAccountLogs())
        .anyMatch(log -> log.getEventType() == PaymentEventType.SETTLEMENT_PAY_PRODUCT_SALES_FEE);
    assertThat(payeeAccount.getPaymentAccountLogs())
        .anyMatch(log -> log.getEventType() == PaymentEventType.SETTLEMENT_PAY_PRODUCT_SALES_FEE);
  }
}
