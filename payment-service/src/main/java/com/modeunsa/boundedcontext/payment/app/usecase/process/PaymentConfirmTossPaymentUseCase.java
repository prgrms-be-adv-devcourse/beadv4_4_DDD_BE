package com.modeunsa.boundedcontext.payment.app.usecase.process;

import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentProcessContext;
import com.modeunsa.boundedcontext.payment.app.dto.toss.TossPaymentsConfirmRequest;
import com.modeunsa.boundedcontext.payment.app.dto.toss.TossPaymentsConfirmResponse;
import com.modeunsa.boundedcontext.payment.app.support.PaymentFailureEventPublisher;
import com.modeunsa.boundedcontext.payment.app.support.PaymentSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.modeunsa.boundedcontext.payment.domain.exception.PaymentErrorCode;
import com.modeunsa.boundedcontext.payment.domain.exception.TossConfirmFailedException;
import com.modeunsa.boundedcontext.payment.domain.exception.TossConfirmRetryableException;
import com.modeunsa.boundedcontext.payment.out.client.TossPaymentClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

@Service
@Transactional(
    noRollbackFor = {
      TossConfirmRetryableException.class,
      TossConfirmFailedException.class,
      ResourceAccessException.class
    })
@RequiredArgsConstructor
public class PaymentConfirmTossPaymentUseCase {

  private final PaymentSupport paymentSupport;
  private final TossPaymentClient tossPaymentClient;
  private final PaymentFailureEventPublisher paymentFailureEventPublisher;

  public PaymentProcessContext execute(PaymentProcessContext context) {

    PaymentId paymentId = PaymentId.create(context.buyerId(), context.orderNo());

    Payment payment = paymentSupport.getPaymentById(paymentId);

    TossPaymentsConfirmRequest tossReq = TossPaymentsConfirmRequest.from(context);

    try {
      TossPaymentsConfirmResponse tossRes = tossPaymentClient.confirmPayment(tossReq);
      payment.changeToApprove(tossRes);
      return PaymentProcessContext.fromPaymentForCharge(payment);
    } catch (TossConfirmRetryableException e) {
      handleTossFailure(context, PaymentErrorCode.PG_TOSS_MAX_RETRY_EXCEEDED, e.getMessage());
      throw e;
    } catch (TossConfirmFailedException e) {
      handleTossFailure(
          context,
          e.getErrorCode(),
          e.getTossMessage() != null ? e.getTossMessage() : e.getMessage());
      throw e;
    } catch (Exception e) {
      handleTossFailure(context, PaymentErrorCode.PG_UNKNOWN_ERROR, e.getMessage());
      throw e;
    }
  }

  private void handleTossFailure(
      PaymentProcessContext context, PaymentErrorCode paymentErrorCode, String message) {
    paymentFailureEventPublisher.publish(
        context.buyerId(),
        context.orderId(),
        context.orderNo(),
        context.totalAmount(),
        paymentErrorCode.getCode(),
        message);
  }
}
