package com.modeunsa.boundedcontext.payment.app.usecase;

import static com.modeunsa.global.status.ErrorStatus.PAYMENT_INSUFFICIENT_BALANCE;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentRequest;
import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.boundedcontext.payment.domain.types.ReferenceType;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import com.modeunsa.shared.payment.event.PaymentFailedEvent;
import com.modeunsa.shared.payment.event.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentCompleteUseCase {

  private final PaymentAccountSupport paymentAccountSupport;
  private final SpringDomainEventPublisher eventPublisher;

  public void completePayment(PaymentRequest paymentRequest) {

    PaymentAccount buyerAccount =
        paymentAccountSupport.getPaymentAccountByMemberId(paymentRequest.getBuyerId());

    if (paymentRequest.isPositive()) {
      buyerAccount.credit(
          paymentRequest.getPgPaymentAmount(),
          PaymentEventType.CHARGE_PG_TOSS_PAYMENTS,
          paymentRequest.getOrderId(),
          ReferenceType.ORDER);
    }

    if (!buyerAccount.canPayOrder(paymentRequest.getSalePrice())) {
      eventPublisher.publish(
          new PaymentFailedEvent(
              PAYMENT_INSUFFICIENT_BALANCE.getCode(),
              PAYMENT_INSUFFICIENT_BALANCE.getMessage(),
              paymentRequest.getOrderId(),
              paymentRequest.getPgPaymentAmount(),
              buyerAccount.getShortFailAmount(paymentRequest.getPgPaymentAmount())));
      return;
    }

    buyerAccount.debit(
        paymentRequest.getSalePrice(),
        PaymentEventType.USE_ORDER_PAYMENT,
        paymentRequest.getOrderId(),
        ReferenceType.ORDER);

    PaymentAccount holderAccount = paymentAccountSupport.getHolderAccount();

    holderAccount.credit(
        paymentRequest.getSalePrice(),
        PaymentEventType.HOLD_STORE_ORDER_PAYMENT,
        paymentRequest.getOrderId(),
        ReferenceType.ORDER);

    eventPublisher.publish(
        new PaymentSuccessEvent(paymentRequest.getOrderId(), paymentRequest.getPgPaymentAmount()));
  }
}
