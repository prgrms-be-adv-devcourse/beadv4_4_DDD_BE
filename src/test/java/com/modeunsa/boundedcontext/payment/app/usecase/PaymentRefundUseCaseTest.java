package com.modeunsa.boundedcontext.payment.app.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.domain.types.MemberStatus;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.boundedcontext.payment.domain.types.RefundEventType;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.payment.dto.PaymentDto;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentRefundUseCase 테스트")
public class PaymentRefundUseCaseTest {

  @Mock private PaymentAccountSupport paymentAccountSupport;

  @InjectMocks private PaymentRefundUseCase paymentRefundUseCase;

  private PaymentMember buyerMember;
  private PaymentMember holderMember;
  private PaymentAccount holderAccount;
  private PaymentAccount buyerAccount;

  @BeforeEach
  void setUp() {
    holderMember = PaymentMember.create(2L, "holder@example.com", "홀더", MemberStatus.ACTIVE);
    buyerMember = PaymentMember.create(1000L, "user1@example.com", "구매자", MemberStatus.ACTIVE);

    holderAccount = PaymentAccount.create(holderMember);
    holderAccount.credit(BigDecimal.valueOf(100000), PaymentEventType.CHARGE_BANK_TRANSFER);

    buyerAccount = PaymentAccount.create(buyerMember);
  }

  @Test
  @DisplayName("결제 취소 환불 처리 성공")
  void executeRefundPaymentFailed() {
    // given
    PaymentDto request =
        PaymentDto.builder()
            .orderId(1L)
            .orderNo("ORDER12345")
            .buyerId(buyerMember.getId())
            .pgPaymentAmount(BigDecimal.valueOf(5000))
            .build();

    when(paymentAccountSupport.getHolderAccount()).thenReturn(holderAccount);
    when(paymentAccountSupport.getPaymentAccountByMemberId(buyerMember.getId()))
        .thenReturn(buyerAccount);

    BigDecimal holderBalanceBefore = holderAccount.getBalance();
    BigDecimal buyerBalanceBefore = buyerAccount.getBalance();

    // when
    paymentRefundUseCase.execute(request, RefundEventType.PAYMENT_FAILED);

    // then
    assertThat(holderAccount.getBalance())
        .isEqualByComparingTo(holderBalanceBefore.subtract(request.getPgPaymentAmount()));
    assertThat(buyerAccount.getBalance())
        .isEqualByComparingTo(buyerBalanceBefore.add(request.getPgPaymentAmount()));
  }

  @Test
  @DisplayName("주문 취소 환불 처리 성공")
  void executeRefundOrderCanceled() {
    // given
    PaymentDto request =
        PaymentDto.builder()
            .orderId(1L)
            .orderNo("ORDER12345")
            .buyerId(buyerMember.getId())
            .pgPaymentAmount(BigDecimal.valueOf(5000))
            .build();

    when(paymentAccountSupport.getHolderAccount()).thenReturn(holderAccount);
    when(paymentAccountSupport.getPaymentAccountByMemberId(buyerMember.getId()))
        .thenReturn(buyerAccount);

    BigDecimal holderBalanceBefore = holderAccount.getBalance();
    BigDecimal buyerBalanceBefore = buyerAccount.getBalance();

    // when
    paymentRefundUseCase.execute(request, RefundEventType.ORDER_CANCELLED);

    // then
    assertThat(holderAccount.getBalance())
        .isEqualByComparingTo(holderBalanceBefore.subtract(request.getPgPaymentAmount()));
    assertThat(buyerAccount.getBalance())
        .isEqualByComparingTo(buyerBalanceBefore.add(request.getPgPaymentAmount()));
  }

  @Test
  @DisplayName("환불 처리 실패 - 홀더 계좌 잔액 부족")
  void executeRefundFailureInsufficientBalance() {
    // given
    PaymentAccount insufficientHolderAccount = PaymentAccount.create(holderMember);
    insufficientHolderAccount.credit(
        BigDecimal.valueOf(3000), PaymentEventType.CHARGE_BANK_TRANSFER);

    PaymentDto request =
        PaymentDto.builder()
            .orderId(1L)
            .orderNo("ORDER12345")
            .buyerId(buyerMember.getId())
            .pgPaymentAmount(BigDecimal.valueOf(5000))
            .build();

    when(paymentAccountSupport.getHolderAccount()).thenReturn(insufficientHolderAccount);

    // when, then
    assertThatThrownBy(() -> paymentRefundUseCase.execute(request, RefundEventType.PAYMENT_FAILED))
        .isInstanceOf(GeneralException.class)
        .extracting("errorStatus")
        .isEqualTo(ErrorStatus.PAYMENT_INSUFFICIENT_BALANCE);
  }
}
