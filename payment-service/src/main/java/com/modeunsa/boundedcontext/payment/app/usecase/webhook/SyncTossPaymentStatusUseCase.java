package com.modeunsa.boundedcontext.payment.app.usecase.webhook;

import com.modeunsa.boundedcontext.payment.app.dto.toss.TossWebhookRequest.TossWebhookData;
import com.modeunsa.boundedcontext.payment.app.support.PaymentSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.exception.PaymentErrorCode;
import com.modeunsa.boundedcontext.payment.domain.exception.TossWebhookErrorCode;
import com.modeunsa.boundedcontext.payment.domain.exception.TossWebhookException;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.shared.payment.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SyncTossPaymentStatusUseCase {

  private final PaymentSupport paymentSupport;
  private final EventPublisher eventPublisher;

  public void execute(TossWebhookData data) {

    Payment payment = paymentSupport.getPaymentByOrderNo(data.orderId());

    switch (data.status()) {
      case READY, WAITING_FOR_DEPOSIT -> {}
      case IN_PROGRESS -> payment.syncToInProgress();
      case DONE -> payment.syncToApproved();
      case CANCELED, PARTIAL_CANCELED -> payment.syncToCanceled();
      case ABORTED -> handleFailure(payment, PaymentErrorCode.PG_PAYMENT_ABORTED, data.failure());
      case EXPIRED -> handleFailure(payment, PaymentErrorCode.PG_PAYMENT_EXPIRED, data.failure());
      default -> throw new TossWebhookException(TossWebhookErrorCode.INVALID_EVENT_TYPE);
    }
  }

  private void handleFailure(
      Payment payment, PaymentErrorCode paymentErrorCode, TossWebhookData.FailureInfo failure) {
    eventPublisher.publish(
        PaymentFailedEvent.from(
            payment.getId().getMemberId(),
            payment.getOrderId(),
            payment.getId().getOrderNo(),
            payment.getTotalAmount(),
            paymentErrorCode.getCode(),
            failure.message()));
  }
}
