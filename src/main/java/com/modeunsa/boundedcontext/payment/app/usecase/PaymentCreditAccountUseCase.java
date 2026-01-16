package com.modeunsa.boundedcontext.payment.app.usecase;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentAccountDepositRequest;
import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentCreditAccountUseCase {

  private final PaymentAccountSupport paymentAccountSupport;

  public BigDecimal execute(PaymentAccountDepositRequest paymentAccountDepositRequest) {
    var paymentAccount =
        paymentAccountSupport.getPaymentAccountByMemberId(
            paymentAccountDepositRequest.getMemberId());
    paymentAccount.credit(
        paymentAccountDepositRequest.getAmount(),
        paymentAccountDepositRequest.getPaymentEventType());
    return paymentAccount.getBalance();
  }
}
