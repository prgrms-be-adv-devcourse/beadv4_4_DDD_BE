package com.modeunsa.boundedcontext.payment.app.usecase.account;

import com.modeunsa.boundedcontext.payment.app.dto.account.PaymentAccountDepositRequest;
import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentCreditAccountUseCase {

  private final PaymentAccountSupport paymentAccountSupport;

  public BigDecimal execute(
      Long memberId, PaymentAccountDepositRequest paymentAccountDepositRequest) {
    var paymentAccount = paymentAccountSupport.getPaymentAccountByMemberId(memberId);
    paymentAccount.credit(
        paymentAccountDepositRequest.amount(), paymentAccountDepositRequest.paymentEventType());
    return paymentAccount.getBalance();
  }
}
