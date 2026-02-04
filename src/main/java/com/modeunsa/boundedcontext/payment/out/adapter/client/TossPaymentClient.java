package com.modeunsa.boundedcontext.payment.out.adapter.client;

import com.modeunsa.boundedcontext.payment.app.dto.toss.TossPaymentsConfirmRequest;
import com.modeunsa.boundedcontext.payment.app.dto.toss.TossPaymentsConfirmResponse;

public interface TossPaymentClient {
  TossPaymentsConfirmResponse confirmPayment(TossPaymentsConfirmRequest confirmPaymentRequest);
}
