package com.modeunsa.boundedcontext.payment.app.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentRequestResult;
import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.app.support.PaymentSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.domain.types.MemberStatus;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCompleteUseCase 테스트")
public class PaymentProcessUseCaseTest {

  @Mock private PaymentAccountSupport paymentAccountSupport;
  @Mock private PaymentSupport paymentSupport;
  @Mock private SpringDomainEventPublisher eventPublisher;

  @InjectMocks private PaymentProcessUseCase paymentProcessUseCase;

  private PaymentMember buyerMember;
  private PaymentMember holderMember;
  private PaymentAccount holderAccount;
  private PaymentAccount buyerAccount;

  @BeforeEach
  void setUp() {
    holderMember = PaymentMember.create(2L, "holder@example.com", "홀더", MemberStatus.ACTIVE);
    buyerMember = PaymentMember.create(1000L, "user1@example.com", "구매자", MemberStatus.ACTIVE);

    holderAccount = PaymentAccount.create(holderMember);

    buyerAccount = PaymentAccount.create(buyerMember);
    buyerAccount.credit(BigDecimal.valueOf(100000), PaymentEventType.CHARGE_BANK_TRANSFER);
  }

  @Test
  @DisplayName("PG 충전 없이 결제 처리")
  void executeCompletePayment() {
    // given
    PaymentRequestResult paymentRequestResult =
        new PaymentRequestResult(
            buyerMember.getId(),
            "ORDER12345",
            1L,
            false,
            BigDecimal.ZERO,
            BigDecimal.valueOf(20000));

    when(paymentAccountSupport.getHolderAccount()).thenReturn(holderAccount);
    when(paymentAccountSupport.getPaymentAccountByMemberId(buyerMember.getId()))
        .thenReturn(buyerAccount);

    BigDecimal holderBalanceBefore = holderAccount.getBalance();
    BigDecimal buyerBalanceBefore = buyerAccount.getBalance();

    // when
    paymentProcessUseCase.execute(paymentRequestResult);

    // then
    assertThat(buyerAccount.getBalance())
        .isEqualByComparingTo(buyerBalanceBefore.subtract(paymentRequestResult.getTotalAmount()));
    assertThat(holderAccount.getBalance())
        .isEqualByComparingTo(holderBalanceBefore.add(paymentRequestResult.getTotalAmount()));
  }
}
