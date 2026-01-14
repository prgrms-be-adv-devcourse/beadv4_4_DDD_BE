package com.modeunsa.boundedcontext.payment.app.usecase;

import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentCreditAccountUseCase {

  private final PaymentAccountSupport paymentAccountSupport;

  public void execute(Long memberId, BigDecimal amount, PaymentEventType paymentEventType) {
    var paymentAccount = paymentAccountSupport.getPaymentAccountByMemberId(memberId);
    paymentAccount.credit(amount, paymentEventType);
  }
}
