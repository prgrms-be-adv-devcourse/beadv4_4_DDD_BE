package com.modeunsa.boundedcontext.payment.app.usecase;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentPayoutDto;
import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.boundedcontext.payment.domain.types.ReferenceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentPayoutCompleteUseCase {

  private final PaymentAccountSupport paymentAccountSupport;

  public void execute(PaymentPayoutDto payout) {

    PaymentEventType eventType = PaymentEventType.fromPayoutEventType(payout.getPayoutEventType());

    PaymentAccount holderAccount = paymentAccountSupport.getHolderAccount();

    holderAccount.debit(payout.getAmount(), eventType, payout.getId(), ReferenceType.PAYOUT);

    PaymentAccount payeeAccount =
        paymentAccountSupport.getPaymentAccountByMemberId(payout.getPayeeId());

    payeeAccount.credit(payout.getAmount(), eventType, payout.getId(), ReferenceType.PAYOUT);
  }
}
