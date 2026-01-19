package com.modeunsa.boundedcontext.payment.app.usecase;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentProcessContext;
import com.modeunsa.boundedcontext.payment.app.lock.LockedPaymentAccounts;
import com.modeunsa.boundedcontext.payment.app.lock.PaymentAccountLockManager;
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

  private final PaymentSupport paymentSupport;
  private final PaymentAccountLockManager paymentAccountLockManager;
  private final SpringDomainEventPublisher eventPublisher;
  private final PaymentAccountConfig paymentAccountConfig;

  public void execute(PaymentProcessContext paymentProcessContext) {

    // 1. 결제 계좌에 대한 Lock 획득
    LockedPaymentAccounts accounts =
        paymentAccountLockManager.getEntitiesForUpdateInOrder(
            paymentAccountConfig.getHolderMemberId(), paymentProcessContext.getBuyerId());

    // 2. 결제 계좌 영속성 획득
    PaymentAccount holderAccount = accounts.get(paymentAccountConfig.getHolderMemberId());
    PaymentAccount buyerAccount = accounts.get(paymentProcessContext.getBuyerId());

    // 3. 결제 처리
    if (paymentProcessContext.isNeedsCharge()) {
      executeWithCharge(holderAccount, buyerAccount, paymentProcessContext);
    } else {
      executeWithoutCharge(holderAccount, buyerAccount, paymentProcessContext);
    }
  }

  private void executeWithoutCharge(
      PaymentAccount holderAccount,
      PaymentAccount buyerAccount,
      PaymentProcessContext paymentProcessContext) {
    processPayment(holderAccount, buyerAccount, paymentProcessContext);
  }

  private void executeWithCharge(
      PaymentAccount holderAccount,
      PaymentAccount buyerAccount,
      PaymentProcessContext paymentProcessContext) {
    chargeFromPg(buyerAccount, paymentProcessContext);
    processPayment(holderAccount, buyerAccount, paymentProcessContext);
  }

  private void chargeFromPg(
      PaymentAccount buyerAccount, PaymentProcessContext paymentProcessContext) {
    buyerAccount.credit(
        paymentProcessContext.getChargeAmount(),
        PaymentEventType.CHARGE_PG_TOSS_PAYMENTS,
        paymentProcessContext.getOrderId(),
        ReferenceType.ORDER);
  }

  private void processPayment(
      PaymentAccount holderAccount,
      PaymentAccount buyerAccount,
      PaymentProcessContext paymentProcessContext) {
    buyerAccount.debit(
        paymentProcessContext.getTotalAmount(),
        PaymentEventType.USE_ORDER_PAYMENT,
        paymentProcessContext.getOrderId(),
        ReferenceType.ORDER);

    holderAccount.credit(
        paymentProcessContext.getTotalAmount(),
        PaymentEventType.HOLD_STORE_ORDER_PAYMENT,
        paymentProcessContext.getOrderId(),
        ReferenceType.ORDER);

    paymentSupport.changePaymentStatus(
        paymentProcessContext.getBuyerId(),
        paymentProcessContext.getOrderNo(),
        PaymentStatus.COMPLETED);

    publishPaymentSuccessEvent(paymentProcessContext);
  }

  private void publishPaymentSuccessEvent(PaymentProcessContext paymentProcessContext) {
    eventPublisher.publish(
        new PaymentSuccessEvent(
            new PaymentDto(
                paymentProcessContext.getOrderId(),
                paymentProcessContext.getOrderNo(),
                paymentProcessContext.getBuyerId(),
                paymentProcessContext.getTotalAmount())));
  }
}
