package com.modeunsa.boundedcontext.payment.app.usecase;

import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.boundedcontext.payment.domain.types.ReferenceType;
import com.modeunsa.boundedcontext.payment.domain.types.RefundEventType;
import com.modeunsa.shared.payment.dto.PaymentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentRefundUseCase {

  private final PaymentAccountSupport paymentAccountSupport;

  public void execute(PaymentDto payment, RefundEventType refundEventType) {

    PaymentEventType eventType = PaymentEventType.fromRefundEventType(refundEventType);

    PaymentAccount holderAccount = paymentAccountSupport.getHolderAccount();
    PaymentAccount buyerAccount =
        paymentAccountSupport.getPaymentAccountByMemberId(payment.getBuyerId());

    holderAccount.debit(
        payment.getPgPaymentAmount(), eventType, payment.getOrderId(), ReferenceType.PAYMENT);
    buyerAccount.credit(
        payment.getPgPaymentAmount(), eventType, payment.getOrderId(), ReferenceType.PAYMENT);
  }
}
