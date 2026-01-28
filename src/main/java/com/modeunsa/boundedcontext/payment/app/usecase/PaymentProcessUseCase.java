package com.modeunsa.boundedcontext.payment.app.usecase;

import co.elastic.clients.util.VisibleForTesting;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentProcessContext;
import com.modeunsa.boundedcontext.payment.app.lock.LockedPaymentAccounts;
import com.modeunsa.boundedcontext.payment.app.lock.PaymentAccountLockManager;
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

  private final PaymentSupport paymentSupport;
  private final PaymentAccountSupport paymentAccountSupport;
  private final PaymentAccountLockManager paymentAccountLockManager;
  private final SpringDomainEventPublisher eventPublisher;
  private final PaymentAccountConfig paymentAccountConfig;

  public void execute(PaymentProcessContext paymentProcessContext) {

    // 1. 결제 계좌에 대한 Lock 획득
    LockedPaymentAccounts accounts =
        paymentAccountLockManager.getEntitiesForUpdateInOrder(
            paymentAccountConfig.getHolderMemberId(), paymentProcessContext.buyerId());

    // 2. 결제 계좌 영속성 획득
    PaymentAccount holderAccount = accounts.get(paymentAccountConfig.getHolderMemberId());
    PaymentAccount buyerAccount = accounts.get(paymentProcessContext.buyerId());

    // 3. 결제 처리
    if (paymentProcessContext.needsCharge()) {
      processWithCharge(holderAccount, buyerAccount, paymentProcessContext);
    } else {
      processWithoutCharge(holderAccount, buyerAccount, paymentProcessContext);
    }
  }

  @VisibleForTesting
  public void executeWithoutLock(PaymentProcessContext paymentProcessContext) {

    // 1. 결제 계좌 영속성 획득 (Lock 미획득)
    PaymentAccount holderAccount = paymentAccountSupport.getHolderAccount();
    PaymentAccount buyerAccount =
        paymentAccountSupport.getPaymentAccountByMemberId(paymentProcessContext.buyerId());

    // 2. 결제 처리
    if (paymentProcessContext.needsCharge()) {
      processWithCharge(holderAccount, buyerAccount, paymentProcessContext);
    } else {
      processWithoutCharge(holderAccount, buyerAccount, paymentProcessContext);
    }
  }

  private void processWithoutCharge(
      PaymentAccount holderAccount,
      PaymentAccount buyerAccount,
      PaymentProcessContext paymentProcessContext) {
    processPayment(holderAccount, buyerAccount, paymentProcessContext);
  }

  private void processWithCharge(
      PaymentAccount holderAccount,
      PaymentAccount buyerAccount,
      PaymentProcessContext paymentProcessContext) {
    chargeFromPg(buyerAccount, paymentProcessContext);
    processPayment(holderAccount, buyerAccount, paymentProcessContext);
  }

  private void chargeFromPg(
      PaymentAccount buyerAccount, PaymentProcessContext paymentProcessContext) {
    buyerAccount.credit(
        paymentProcessContext.chargeAmount(),
        PaymentEventType.CHARGE_PG_TOSS_PAYMENTS,
        paymentProcessContext.orderId(),
        ReferenceType.ORDER);
  }

  private void processPayment(
      PaymentAccount holderAccount,
      PaymentAccount buyerAccount,
      PaymentProcessContext paymentProcessContext) {
    buyerAccount.debit(
        paymentProcessContext.totalAmount(),
        PaymentEventType.USE_ORDER_PAYMENT,
        paymentProcessContext.orderId(),
        ReferenceType.ORDER);

    holderAccount.credit(
        paymentProcessContext.totalAmount(),
        PaymentEventType.HOLD_STORE_ORDER_PAYMENT,
        paymentProcessContext.orderId(),
        ReferenceType.ORDER);

    paymentSupport.changePaymentStatus(
        paymentProcessContext.buyerId(), paymentProcessContext.orderNo(), PaymentStatus.COMPLETED);

    publishPaymentSuccessEvent(paymentProcessContext);
  }

  private void publishPaymentSuccessEvent(PaymentProcessContext paymentProcessContext) {
    eventPublisher.publish(
        new PaymentSuccessEvent(
            PaymentDto.of(
                paymentProcessContext.orderId(),
                paymentProcessContext.orderNo(),
                paymentProcessContext.buyerId(),
                paymentProcessContext.totalAmount())));
  }
}
