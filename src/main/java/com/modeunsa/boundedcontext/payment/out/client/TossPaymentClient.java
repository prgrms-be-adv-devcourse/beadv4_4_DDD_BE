package com.modeunsa.boundedcontext.payment.out.client;

import com.modeunsa.boundedcontext.payment.app.dto.toss.TossPaymentsConfirmRequest;
import java.util.Map;

public interface TossPaymentClient {
  Map<String, Object> confirmPayment(TossPaymentsConfirmRequest confirmPaymentRequest);
}
