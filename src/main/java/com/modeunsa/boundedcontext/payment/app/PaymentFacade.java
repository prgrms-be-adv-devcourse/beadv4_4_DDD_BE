package com.modeunsa.boundedcontext.payment.app;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentMemberDto;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentCreateAccountUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentCreditAccountUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentSyncMemberUseCase;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author : JAKE
 * @date : 26. 1. 13.
 */
@Service
@RequiredArgsConstructor
public class PaymentFacade {

  private final PaymentSyncMemberUseCase paymentSyncMemberUseCase;
  private final PaymentCreateAccountUseCase paymentCreateAccountUseCase;
  private final PaymentCreditAccountUseCase paymentCreditAccountUseCase;

  public void createPaymentMember(PaymentMemberDto paymentMemberDto) {
    paymentSyncMemberUseCase.createPaymentMember(paymentMemberDto);
  }

  public void createPaymentAccount(Long memberId) {
    paymentCreateAccountUseCase.createPaymentAccount(memberId);
  }

  public void creditAccount(Long memberId, BigDecimal amount, PaymentEventType paymentEventType) {
    paymentCreditAccountUseCase.execute(memberId, amount, paymentEventType);
  }
}
