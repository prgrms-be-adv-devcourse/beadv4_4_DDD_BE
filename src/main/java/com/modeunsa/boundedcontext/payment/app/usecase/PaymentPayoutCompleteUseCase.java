package com.modeunsa.boundedcontext.payment.app.usecase;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentPayoutDto;
import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.boundedcontext.payment.domain.types.ReferenceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentPayoutCompleteUseCase {

  private final PaymentAccountSupport paymentAccountSupport;

  public void execute(PaymentPayoutDto payout) {

    PaymentEventType eventType = PaymentEventType.fromPayoutEventType(payout.payoutEventType());

    PaymentAccount holderAccount = paymentAccountSupport.getHolderAccount();
    PaymentAccount payeeAccount = paymentAccountSupport.getPayeeAccount(payout);

    holderAccount.debit(payout.amount(), eventType, payout.id(), ReferenceType.PAYOUT);
    payeeAccount.credit(payout.amount(), eventType, payout.id(), ReferenceType.PAYOUT);
  }
}
