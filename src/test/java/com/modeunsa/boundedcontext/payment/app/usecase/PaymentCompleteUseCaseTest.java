package com.modeunsa.boundedcontext.payment.app.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentRequest;
import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
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
public class PaymentCompleteUseCaseTest {

  @Mock private PaymentAccountSupport paymentAccountSupport;
  @Mock private SpringDomainEventPublisher eventPublisher;

  @InjectMocks private PaymentCompleteUseCase paymentCompleteUseCase;

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
  @DisplayName("결제 완료 처리 성공")
  void executeCompletePayment() {
    // given
    PaymentRequest request =
        PaymentRequest.builder()
            .orderId(1L)
            .orderNo("ORDER12345")
            .buyerId(buyerMember.getId())
            .pgPaymentAmount(BigDecimal.valueOf(5000))
            .salePrice(BigDecimal.valueOf(5000))
            .build();

    when(paymentAccountSupport.getHolderAccount()).thenReturn(holderAccount);
    when(paymentAccountSupport.getPaymentAccountByMemberId(buyerMember.getId()))
        .thenReturn(buyerAccount);

    BigDecimal holderBalanceBefore = holderAccount.getBalance();
    BigDecimal buyerBalanceBefore = buyerAccount.getBalance();

    // when
    paymentCompleteUseCase.execute(request);

    // then
    assertThat(buyerAccount.getBalance())
        .isEqualByComparingTo(
            buyerBalanceBefore.add(request.getPgPaymentAmount()).subtract(request.getSalePrice()));
    assertThat(holderAccount.getBalance())
        .isEqualByComparingTo(holderBalanceBefore.add(request.getSalePrice()));
  }
}
