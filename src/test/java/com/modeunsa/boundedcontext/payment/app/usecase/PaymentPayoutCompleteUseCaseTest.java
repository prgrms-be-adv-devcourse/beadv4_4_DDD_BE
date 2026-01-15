package com.modeunsa.boundedcontext.payment.app.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentPayoutDto;
import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.domain.types.MemberStatus;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.boundedcontext.payment.domain.types.PayoutEventType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentPayoutCompleteUseCase 테스트")
class PaymentPayoutCompleteUseCaseTest {

  @Mock private PaymentAccountSupport paymentAccountSupport;

  @InjectMocks private PaymentPayoutCompleteUseCase paymentPayoutCompleteUseCase;

  private PaymentMember holderMember;
  private PaymentMember payeeMember;
  private PaymentMember systemMember;
  private PaymentAccount holderAccount;
  private PaymentAccount payeeAccount;
  private PaymentAccount systemAccount;

  @BeforeEach
  void setUp() {
    holderMember = PaymentMember.create(1000L, "user1@example.com", "수령인", MemberStatus.ACTIVE);
    payeeMember = PaymentMember.create(2L, "holder@example.com", "홀더", MemberStatus.ACTIVE);
    systemMember = PaymentMember.create(1L, "system@example.com", "시스템", MemberStatus.ACTIVE);

    holderAccount = PaymentAccount.create(holderMember);
    holderAccount.credit(BigDecimal.valueOf(100000), PaymentEventType.CHARGE_BANK_TRANSFER);

    payeeAccount = PaymentAccount.create(payeeMember);
    systemAccount = PaymentAccount.create(systemMember);
  }

  @Test
  @DisplayName("정산 처리 성공 - FEE 타입: holder에서 차감, system 계좌로 입금")
  void executeSuccessFeeType() {
    // given
    BigDecimal payoutAmount = BigDecimal.valueOf(5000);
    PaymentPayoutDto payout =
        new PaymentPayoutDto(1L, 2L, "판매자", LocalDateTime.now(), payoutAmount, PayoutEventType.FEE);

    when(paymentAccountSupport.getHolderAccount()).thenReturn(holderAccount);
    when(paymentAccountSupport.getPayeeAccount(payout)).thenReturn(systemAccount);

    BigDecimal holderBalanceBefore = holderAccount.getBalance();
    BigDecimal systemBalanceBefore = systemAccount.getBalance();

    // when
    paymentPayoutCompleteUseCase.execute(payout);

    // then
    assertThat(holderAccount.getBalance())
        .isEqualByComparingTo(holderBalanceBefore.subtract(payoutAmount));
    assertThat(systemAccount.getBalance())
        .isEqualByComparingTo(systemBalanceBefore.add(payoutAmount));

    verify(paymentAccountSupport).getHolderAccount();
    verify(paymentAccountSupport).getPayeeAccount(payout);
  }

  @Test
  @DisplayName("정산 처리 성공 - AMOUNT 타입: holder에서 차감, payee 계좌로 입금")
  void executeSuccessAmountType() {
    // given
    BigDecimal payoutAmount = BigDecimal.valueOf(10000);
    PaymentPayoutDto payout =
        new PaymentPayoutDto(
            1L, 2L, "판매자", LocalDateTime.now(), payoutAmount, PayoutEventType.AMOUNT);

    when(paymentAccountSupport.getHolderAccount()).thenReturn(holderAccount);
    when(paymentAccountSupport.getPayeeAccount(payout)).thenReturn(payeeAccount);

    BigDecimal holderBalanceBefore = holderAccount.getBalance();
    BigDecimal payeeBalanceBefore = payeeAccount.getBalance();

    // when
    paymentPayoutCompleteUseCase.execute(payout);

    // then
    assertThat(holderAccount.getBalance())
        .isEqualByComparingTo(holderBalanceBefore.subtract(payoutAmount));
    assertThat(payeeAccount.getBalance())
        .isEqualByComparingTo(payeeBalanceBefore.add(payoutAmount));

    verify(paymentAccountSupport).getHolderAccount();
    verify(paymentAccountSupport).getPayeeAccount(payout);
  }

  @Test
  @DisplayName("정산 처리 - AMOUNT 타입일 때 올바른 PaymentEventType으로 변환되는지 확인")
  void executeVerifyEventTypeConversionAmount() {
    // given
    PaymentPayoutDto payout =
        new PaymentPayoutDto(
            1L, 2L, "판매자", LocalDateTime.now(), BigDecimal.valueOf(10000), PayoutEventType.AMOUNT);

    when(paymentAccountSupport.getHolderAccount()).thenReturn(holderAccount);
    when(paymentAccountSupport.getPayeeAccount(payout)).thenReturn(payeeAccount);

    // when
    paymentPayoutCompleteUseCase.execute(payout);

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
    PaymentPayoutDto payout =
        new PaymentPayoutDto(
            1L, 2L, "판매자", LocalDateTime.now(), BigDecimal.valueOf(2000), PayoutEventType.FEE);

    when(paymentAccountSupport.getHolderAccount()).thenReturn(holderAccount);
    when(paymentAccountSupport.getPayeeAccount(payout)).thenReturn(payeeAccount);

    // when
    paymentPayoutCompleteUseCase.execute(payout);

    // then
    assertThat(holderAccount.getPaymentAccountLogs())
        .anyMatch(log -> log.getEventType() == PaymentEventType.SETTLEMENT_PAY_PRODUCT_SALES_FEE);
    assertThat(payeeAccount.getPaymentAccountLogs())
        .anyMatch(log -> log.getEventType() == PaymentEventType.SETTLEMENT_PAY_PRODUCT_SALES_FEE);
  }
}
