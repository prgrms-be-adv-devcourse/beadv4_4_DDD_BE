package com.modeunsa.boundedcontext.payment.app.usecase;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentRequestResult;
import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.app.support.PaymentSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentStatus;
import com.modeunsa.boundedcontext.payment.domain.types.ReferenceType;
import com.modeunsa.global.config.PaymentAccountConfig;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import com.modeunsa.shared.payment.dto.PaymentDto;
import com.modeunsa.shared.payment.event.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentProcessUseCase {

  private final PaymentAccountSupport paymentAccountSupport;
  private final PaymentSupport paymentSupport;
  private final SpringDomainEventPublisher eventPublisher;
  private final PaymentAccountConfig paymentAccountConfig;

  public void execute(PaymentRequestResult paymentRequestResult) {

    Long buyerId = paymentRequestResult.getBuyerId();
    Long holderId = paymentAccountConfig.getHolderMemberId();

    // 항상 작은 ID 부터 락 획득
    PaymentAccount buyerAccount;
    PaymentAccount holderAccount;

    if (buyerId < holderId) {
      buyerAccount = paymentAccountSupport.getPaymentAccountByMemberIdForUpdate(buyerId);
      holderAccount = paymentAccountSupport.getHolderAccountByMemberIdForUpdate();
    } else {
      holderAccount = paymentAccountSupport.getHolderAccountByMemberIdForUpdate();
      buyerAccount = paymentAccountSupport.getPaymentAccountByMemberIdForUpdate(buyerId);
    }

    if (paymentRequestResult.isNeedsCharge()) {
      executeWithCharge(holderAccount, buyerAccount, paymentRequestResult);
    } else {
      executeWithoutCharge(holderAccount, buyerAccount, paymentRequestResult);
    }
  }

  private void executeWithoutCharge(
      PaymentAccount holderAccount,
      PaymentAccount buyerAccount,
      PaymentRequestResult paymentRequestResult) {
    processPayment(holderAccount, buyerAccount, paymentRequestResult);
  }

  private void executeWithCharge(
      PaymentAccount holderAccount,
      PaymentAccount buyerAccount,
      PaymentRequestResult paymentRequestResult) {
    chargeFromPg(buyerAccount, paymentRequestResult);
    processPayment(holderAccount, buyerAccount, paymentRequestResult);
  }

  private void chargeFromPg(
      PaymentAccount buyerAccount, PaymentRequestResult paymentRequestResult) {
    buyerAccount.credit(
        paymentRequestResult.getChargeAmount(),
        PaymentEventType.CHARGE_PG_TOSS_PAYMENTS,
        paymentRequestResult.getOrderId(),
        ReferenceType.ORDER);
  }

  private void processPayment(
      PaymentAccount holderAccount,
      PaymentAccount buyerAccount,
      PaymentRequestResult paymentRequestResult) {
    buyerAccount.debit(
        paymentRequestResult.getTotalAmount(),
        PaymentEventType.USE_ORDER_PAYMENT,
        paymentRequestResult.getOrderId(),
        ReferenceType.ORDER);

    holderAccount.credit(
        paymentRequestResult.getTotalAmount(),
        PaymentEventType.HOLD_STORE_ORDER_PAYMENT,
        paymentRequestResult.getOrderId(),
        ReferenceType.ORDER);

    paymentSupport.changePaymentStatus(
        paymentRequestResult.getBuyerId(),
        paymentRequestResult.getOrderNo(),
        PaymentStatus.COMPLETED);

    publishPaymentSuccessEvent(paymentRequestResult);
  }

  private void publishPaymentSuccessEvent(PaymentRequestResult paymentRequestResult) {
    eventPublisher.publish(
        new PaymentSuccessEvent(
            new PaymentDto(
                paymentRequestResult.getOrderId(),
                paymentRequestResult.getOrderNo(),
                paymentRequestResult.getBuyerId(),
                paymentRequestResult.getTotalAmount())));
  }
}
