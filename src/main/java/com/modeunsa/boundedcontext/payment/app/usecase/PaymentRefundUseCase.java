package com.modeunsa.boundedcontext.payment.app.usecase;

import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.boundedcontext.payment.domain.types.ReferenceType;
import com.modeunsa.boundedcontext.payment.domain.types.RefundEventType;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
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

    if (!holderAccount.canPayOrder(payment.getTotalAmount())) {
      throw new GeneralException(ErrorStatus.PAYMENT_INSUFFICIENT_BALANCE);
    }

    PaymentAccount buyerAccount =
        paymentAccountSupport.getPaymentAccountByMemberId(payment.getBuyerId());

    holderAccount.debit(
        payment.getTotalAmount(), eventType, payment.getOrderId(), ReferenceType.PAYMENT);
    buyerAccount.credit(
        payment.getTotalAmount(), eventType, payment.getOrderId(), ReferenceType.PAYMENT);
  }
}
