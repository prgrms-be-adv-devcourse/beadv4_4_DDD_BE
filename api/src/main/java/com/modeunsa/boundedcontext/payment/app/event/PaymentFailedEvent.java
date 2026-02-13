package com.modeunsa.boundedcontext.payment.app.event;

import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentProcessContext;
import com.modeunsa.boundedcontext.payment.domain.exception.PaymentErrorCode;
import com.modeunsa.global.event.EventUtils;
import com.modeunsa.global.event.TraceableEvent;
import java.math.BigDecimal;

public record PaymentFailedEvent(
    Long memberId,
    Long orderId,
    String orderNo,
    BigDecimal amount,
    PaymentErrorCode errorCode,
    String failureMessage,
    String traceId)
    implements TraceableEvent {

  public static final String EVENT_NAME = "PaymentFailedEvent";

  public PaymentFailedEvent(
      Long memberId,
      Long orderId,
      String orderNo,
      BigDecimal amount,
      PaymentErrorCode errorCode,
      String failureMessage) {
    this(
        memberId, orderId, orderNo, amount, errorCode, failureMessage, EventUtils.extractTraceId());
  }

  public static PaymentFailedEvent from(
      PaymentProcessContext context, PaymentErrorCode errorCode, String failureMessage) {
    return new PaymentFailedEvent(
        context.buyerId(),
        context.orderId(),
        context.orderNo(),
        context.totalAmount(),
        errorCode,
        failureMessage);
  }

  @Override
  public String eventName() {
    return EVENT_NAME;
  }
}
