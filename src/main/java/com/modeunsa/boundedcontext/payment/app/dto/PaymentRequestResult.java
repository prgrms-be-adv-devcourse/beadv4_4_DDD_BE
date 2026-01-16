package com.modeunsa.boundedcontext.payment.app.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentRequestResult {
  private final Long buyerId;
  private final String orderNo;
  private final Long orderId;
  private final boolean needsCharge;
  private final BigDecimal chargeAmount;
  private final BigDecimal totalAmount;
}
