package com.modeunsa.boundedcontext.payment.app.usecase;

import com.modeunsa.boundedcontext.payment.app.dto.order.PaymentOrderInfo;
import com.modeunsa.boundedcontext.payment.app.lock.LockedPaymentAccounts;
import com.modeunsa.boundedcontext.payment.app.lock.PaymentAccountLockManager;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.boundedcontext.payment.domain.types.ReferenceType;
import com.modeunsa.boundedcontext.payment.domain.types.RefundEventType;
import com.modeunsa.global.config.PaymentAccountConfig;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.payment.dto.PaymentDto;
import com.modeunsa.shared.payment.event.PaymentRefundSuccessEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentRefundUseCase {

  private final PaymentAccountLockManager paymentAccountLockManager;
  private final PaymentAccountConfig paymentAccountConfig;
  private final EventPublisher eventPublisher;

  public void execute(PaymentOrderInfo orderInfo, RefundEventType refundEventType) {

    LockedPaymentAccounts accounts =
        paymentAccountLockManager.getEntitiesForUpdateInOrder(
            paymentAccountConfig.getHolderMemberId(), orderInfo.memberId());

    PaymentAccount holderAccount = accounts.get(paymentAccountConfig.getHolderMemberId());
    PaymentAccount buyerAccount = accounts.get(orderInfo.memberId());
    PaymentEventType eventType = PaymentEventType.fromRefundEventType(refundEventType);

    if (!holderAccount.canPayOrder(orderInfo.totalAmount())) {
      throw new GeneralException(ErrorStatus.PAYMENT_INSUFFICIENT_BALANCE);
    }

    holderAccount.debit(
        orderInfo.totalAmount(), eventType, orderInfo.orderId(), ReferenceType.ORDER);
    buyerAccount.credit(
        orderInfo.totalAmount(), eventType, orderInfo.orderId(), ReferenceType.ORDER);

    publishRefundSuccessEvent(orderInfo);
  }

  private void publishRefundSuccessEvent(PaymentOrderInfo orderInfo) {
    eventPublisher.publish(
        new PaymentRefundSuccessEvent(
            new PaymentDto(
                orderInfo.orderId(),
                orderInfo.orderNo(),
                orderInfo.memberId(),
                orderInfo.totalAmount())));
  }
}
