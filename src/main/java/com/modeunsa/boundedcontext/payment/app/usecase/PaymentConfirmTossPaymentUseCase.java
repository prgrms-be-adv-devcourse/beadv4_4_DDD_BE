package com.modeunsa.boundedcontext.payment.app.usecase;

import com.modeunsa.boundedcontext.payment.app.dto.ConfirmPaymentRequest;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentProcessContext;
import com.modeunsa.boundedcontext.payment.app.dto.toss.TossPaymentsConfirmRequest;
import com.modeunsa.boundedcontext.payment.app.dto.toss.TossPaymentsConfirmResponse;
import com.modeunsa.boundedcontext.payment.app.support.PaymentSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.modeunsa.boundedcontext.payment.out.client.TossPaymentClient;
import com.modeunsa.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentConfirmTossPaymentUseCase {

  private final PaymentSupport paymentSupport;
  private final TossPaymentClient tossPaymentClient;

  public PaymentProcessContext execute(
      String orderNo, ConfirmPaymentRequest confirmPaymentRequest) {

    PaymentId paymentId = PaymentId.create(confirmPaymentRequest.memberId(), orderNo);

    Payment payment = paymentSupport.getPaymentById(paymentId);

    TossPaymentsConfirmRequest tossReq = TossPaymentsConfirmRequest.from(confirmPaymentRequest);

    try {
      TossPaymentsConfirmResponse tossRes = tossPaymentClient.confirmPayment(tossReq);
      payment.approveTossPayment(tossRes);
      return PaymentProcessContext.fromPaymentForCharge(payment);
    } catch (GeneralException ge) {
      payment.failedTossPayment(
          ge.getErrorStatus().getHttpStatus(), ge.getErrorStatus().getMessage());
      throw ge;
    } catch (Exception e) {
      payment.failedTossPayment(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
      throw e;
    }
  }
}
