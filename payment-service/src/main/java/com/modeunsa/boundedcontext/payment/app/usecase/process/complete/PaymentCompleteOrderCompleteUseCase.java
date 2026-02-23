package com.modeunsa.boundedcontext.payment.app.usecase.process.complete;

import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentProcessContext;
import com.modeunsa.boundedcontext.payment.app.lock.LockedPaymentAccounts;
import com.modeunsa.boundedcontext.payment.app.lock.PaymentAccountLockManager;
import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.app.support.PaymentSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentPurpose;
import com.modeunsa.boundedcontext.payment.domain.types.ReferenceType;
import com.modeunsa.global.aop.saga.OrderSagaStep;
import com.modeunsa.global.aop.saga.SagaStep;
import com.modeunsa.global.aop.saga.SagaType;
import com.modeunsa.global.config.PaymentAccountConfig;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.global.retry.RetryOnDbFailure;
import com.modeunsa.shared.payment.dto.PaymentDto;
import com.modeunsa.shared.payment.event.PaymentSuccessEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentCompleteOrderCompleteUseCase implements PaymentCompleteProcess {

  private final PaymentSupport paymentSupport;
  private final PaymentAccountSupport paymentAccountSupport;
  private final PaymentAccountLockManager paymentAccountLockManager;
  private final EventPublisher eventPublisher;
  private final PaymentAccountConfig paymentAccountConfig;

  @Override
  public PaymentPurpose purpose() {
    return PaymentPurpose.PRODUCT_PURCHASE;
  }

  @SagaStep(sagaName = SagaType.ORDER_FLOW, step = OrderSagaStep.PAYMENT_SUCCESS)
  @Override
  @RetryOnDbFailure
  public void execute(PaymentProcessContext paymentProcessContext) {

    // 1. 결제 계좌에 대한 Lock 획득
    LockedPaymentAccounts accounts =
        paymentAccountLockManager.getEntitiesForUpdateInOrder(
            List.of(paymentAccountConfig.getHolderMemberId(), paymentProcessContext.buyerId()));

    // 2. 결제 계좌 영속성 획득
    PaymentAccount holderAccount = accounts.get(paymentAccountConfig.getHolderMemberId());
    PaymentAccount buyerAccount = accounts.get(paymentProcessContext.buyerId());

    // 3. 결제 처리
    if (paymentProcessContext.needsPgPayment()) {
      processWithCharge(holderAccount, buyerAccount, paymentProcessContext);
    } else {
      processWithoutCharge(holderAccount, buyerAccount, paymentProcessContext);
    }
  }

  // 테스트 용도: Lock 없이 결제 처리 로직을 검증하기 위한 메서드
  public void executeWithoutLock(PaymentProcessContext paymentProcessContext) {

    // 1. 결제 계좌 영속성 획득 (Lock 미획득)
    PaymentAccount holderAccount = paymentAccountSupport.getHolderAccount();
    PaymentAccount buyerAccount =
        paymentAccountSupport.getPaymentAccountByMemberId(paymentProcessContext.buyerId());

    // 2. 결제 처리
    if (paymentProcessContext.needsPgPayment()) {
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
    paymentAccountSupport.creditIdempotent(
        buyerAccount,
        paymentProcessContext.requestPgAmount(),
        PaymentEventType.CHARGE_PG_TOSS_PAYMENTS,
        ReferenceType.ORDER,
        paymentProcessContext.orderId());
  }

  private void processPayment(
      PaymentAccount holderAccount, PaymentAccount buyerAccount, PaymentProcessContext context) {

    paymentAccountSupport.debitIdempotent(
        buyerAccount,
        context.totalAmount(),
        PaymentEventType.USE_ORDER_PAYMENT,
        ReferenceType.ORDER,
        context.orderId());

    paymentAccountSupport.creditIdempotent(
        holderAccount,
        context.totalAmount(),
        PaymentEventType.HOLD_STORE_ORDER_PAYMENT,
        ReferenceType.ORDER,
        context.orderId());

    Payment payment = loadPayment(context.buyerId(), context.orderNo());
    payment.changeToSuccess();

    publishPaymentSuccessEvent(context);
  }

  private Payment loadPayment(Long memberId, String orderNo) {
    PaymentId paymentId = PaymentId.create(memberId, orderNo);
    return paymentSupport.getPaymentById(paymentId);
  }

  private void publishPaymentSuccessEvent(PaymentProcessContext paymentProcessContext) {
    eventPublisher.publish(
        new PaymentSuccessEvent(
            new PaymentDto(
                paymentProcessContext.orderId(),
                paymentProcessContext.orderNo(),
                paymentProcessContext.buyerId(),
                paymentProcessContext.totalAmount())));
  }
}
