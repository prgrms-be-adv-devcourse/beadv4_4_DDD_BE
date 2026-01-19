package com.modeunsa.boundedcontext.payment.app.usecase;

import com.modeunsa.boundedcontext.payment.app.dto.ConfirmPaymentRequest;
import com.modeunsa.boundedcontext.payment.app.dto.toss.TossPaymentsConfirmRequest;
import com.modeunsa.boundedcontext.payment.out.client.TossPaymentClient;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentConfirmTossPaymentUseCase {

  private final TossPaymentClient tossPaymentClient;

  public Map<String, Object> confirmCardPayment(
      String orderNo, ConfirmPaymentRequest confirmPaymentRequest) {

    TossPaymentsConfirmRequest tossPaymentsConfirmRequest =
        new TossPaymentsConfirmRequest(
            confirmPaymentRequest.paymentKey(),
            confirmPaymentRequest.orderId(),
            confirmPaymentRequest.amount());

    return tossPaymentClient.confirmPayment(tossPaymentsConfirmRequest);
  }
}
