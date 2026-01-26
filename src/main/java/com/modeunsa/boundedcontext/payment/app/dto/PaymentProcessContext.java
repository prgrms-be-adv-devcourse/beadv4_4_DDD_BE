package com.modeunsa.boundedcontext.payment.app.dto;

import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record PaymentProcessContext(
    Long buyerId,
    String orderNo,
    Long orderId,
    boolean needsCharge,
    BigDecimal chargeAmount,
    BigDecimal totalAmount,
    String paymentKey,
    String pgCustomerName,
    String pgCustomerEmail,
    String pgOrderId,
    long pgAmount) {

  public static PaymentProcessContext fromPaymentForInitialize(Payment payment) {
    return PaymentProcessContext.builder()
        .buyerId(payment.getId().getMemberId())
        .orderNo(payment.getId().getOrderNo())
        .orderId(payment.getOrderId())
        .totalAmount(payment.getTotalAmount())
        .needsCharge(false)
        .build();
  }

  public static PaymentProcessContext fromPaymentForInProgress(Payment payment) {
    return PaymentProcessContext.builder()
        .buyerId(payment.getId().getMemberId())
        .orderNo(payment.getId().getOrderNo())
        .orderId(payment.getOrderId())
        .totalAmount(payment.getTotalAmount())
        .needsCharge(payment.isNeedCharge())
        .chargeAmount(payment.getShortAmount())
        .build();
  }

  public static PaymentProcessContext fromConfirmPaymentRequest(
      Long memberId, String orderNo, ConfirmPaymentRequest confirmPaymentRequest) {
    return PaymentProcessContext.builder()
        .buyerId(memberId)
        .orderId(Long.valueOf(confirmPaymentRequest.orderId()))
        .orderNo(orderNo)
        .needsCharge(true)
        .chargeAmount(BigDecimal.valueOf(confirmPaymentRequest.amount()))
        .paymentKey(confirmPaymentRequest.paymentKey())
        .pgCustomerEmail(confirmPaymentRequest.pgCustomerEmail())
        .pgCustomerName(confirmPaymentRequest.pgCustomerName())
        .pgOrderId(confirmPaymentRequest.orderId())
        .pgAmount(confirmPaymentRequest.amount())
        .build();
  }

  public static PaymentProcessContext fromPaymentForCharge(Payment payment) {
    return PaymentProcessContext.builder()
        .buyerId(payment.getId().getMemberId())
        .orderNo(payment.getId().getOrderNo())
        .orderId(payment.getOrderId())
        .needsCharge(payment.isNeedCharge())
        .chargeAmount(payment.getPgPaymentAmount())
        .totalAmount(payment.getTotalAmount())
        .build();
  }
}
