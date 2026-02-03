package com.modeunsa.boundedcontext.payment.app.dto;

import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.types.ProviderType;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record PaymentProcessContext(
    Long buyerId,
    String orderNo,
    Long orderId,
    boolean needsPgPayment,
    BigDecimal requestPgAmount,
    BigDecimal totalAmount,
    ProviderType providerType,
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
        .needsPgPayment(false)
        .providerType(payment.getPaymentProvider())
        .build();
  }

  public static PaymentProcessContext fromPaymentForInProgress(Payment payment) {
    return PaymentProcessContext.builder()
        .buyerId(payment.getId().getMemberId())
        .orderNo(payment.getId().getOrderNo())
        .orderId(payment.getOrderId())
        .totalAmount(payment.getTotalAmount())
        .needsPgPayment(payment.isNeedPgPayment())
        .requestPgAmount(payment.getRequestPgAmount())
        .providerType(payment.getPaymentProvider())
        .build();
  }

  public static PaymentProcessContext fromConfirmPaymentRequest(
      Long memberId, String orderNo, ConfirmPaymentRequest confirmPaymentRequest) {
    return PaymentProcessContext.builder()
        .buyerId(memberId)
        .orderId(Long.valueOf(confirmPaymentRequest.orderId()))
        .orderNo(orderNo)
        .needsPgPayment(true)
        .requestPgAmount(BigDecimal.valueOf(confirmPaymentRequest.amount()))
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
        .needsPgPayment(payment.isNeedPgPayment())
        .requestPgAmount(payment.getPgPaymentAmount())
        .totalAmount(payment.getTotalAmount())
        .build();
  }
}
