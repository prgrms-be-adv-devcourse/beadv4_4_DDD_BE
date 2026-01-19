package com.modeunsa.boundedcontext.payment.app.dto;

import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
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

  public static PaymentRequestResult fromPaymentForCharge(Payment payment) {
    return new PaymentRequestResult(
        payment.getId().getMemberId(),
        payment.getId().getOrderNo(),
        payment.getOrderId(),
        true,
        payment.getPgPaymentAmount(),
        payment.getTotalAmount());
  }
}
