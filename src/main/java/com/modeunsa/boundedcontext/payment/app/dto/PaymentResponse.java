package com.modeunsa.boundedcontext.payment.app.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentResponse {
  private final Long buyerId;
  private final String orderNo;
  private final Long orderId;
  private final BigDecimal totalAmount;
  private final boolean needsCharge;
  private final BigDecimal chargeAmount;

  public PaymentResponse(PaymentRequestResult result) {
    this.buyerId = result.getBuyerId();
    this.orderNo = result.getOrderNo();
    this.orderId = result.getOrderId();
    this.totalAmount = result.getTotalAmount();
    this.needsCharge = result.isNeedsCharge();
    this.chargeAmount = result.getChargeAmount();
  }
}
