package com.modeunsa.boundedcontext.payment.app.usecase.process;

import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentProcessContext;
import com.modeunsa.boundedcontext.payment.app.support.PaymentFailureEventPublisher;
import com.modeunsa.boundedcontext.payment.app.usecase.process.complete.PaymentCompleteRegistry;
import com.modeunsa.boundedcontext.payment.domain.exception.PaymentErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentCompleteUseCase {

  private final PaymentCompleteRegistry paymentCompleteRegistry;
  private final PaymentFailureEventPublisher paymentFailedEventPublisher;

  public void execute(PaymentProcessContext context) {
    try {
      paymentCompleteRegistry.get(context.paymentPurpose()).execute(context);
    } catch (Exception e) {
      handleFailure(context, e);
      throw e;
    }
  }

  private void handleFailure(PaymentProcessContext context, Exception e) {
    String message =
        e.getMessage() != null && !e.getMessage().isBlank()
            ? e.getMessage()
            : PaymentErrorCode.PAYMENT_COMPLETE_FAILED.getMessage();
    paymentFailedEventPublisher.publish(
        context.buyerId(),
        context.orderId(),
        context.orderNo(),
        context.totalAmount(),
        PaymentErrorCode.PAYMENT_COMPLETE_FAILED.getCode(),
        message);
  }
}
