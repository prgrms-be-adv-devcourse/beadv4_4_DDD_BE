package com.modeunsa.boundedcontext.payment.app.dto;

import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import java.math.BigDecimal;

public record PaymentProcessContext(
    Long buyerId,
    String orderNo,
    Long orderId,
    boolean needsCharge,
    BigDecimal chargeAmount,
    BigDecimal totalAmount) {

  public static PaymentProcessContext fromPaymentForCharge(Payment payment) {
    return new PaymentProcessContext(
        payment.getId().getMemberId(),
        payment.getId().getOrderNo(),
        payment.getOrderId(),
        true,
        payment.getPgPaymentAmount(),
        payment.getTotalAmount());
  }
}
