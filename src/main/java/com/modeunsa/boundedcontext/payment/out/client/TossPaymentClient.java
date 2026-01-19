package com.modeunsa.boundedcontext.payment.out.client;

import com.modeunsa.boundedcontext.payment.app.dto.ConfirmPaymentRequest;
import java.util.Map;

public interface TossPaymentClient {
  Map<String, Object> confirmPayment(String orderNo, ConfirmPaymentRequest confirmPaymentRequest);
}
