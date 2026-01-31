package com.modeunsa.boundedcontext.payment.app.event;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentProcessContext;
import com.modeunsa.boundedcontext.payment.domain.exception.PaymentErrorCode;
import com.modeunsa.global.event.EventUtils;
import com.modeunsa.global.event.TraceableEvent;
import java.math.BigDecimal;

public record PaymentFailedEvent(
    Long memberId,
    Long orderId,
    String orderNo,
    BigDecimal amount,
    PaymentErrorCode failureReason,
    String traceId)
    implements TraceableEvent {

  public static final String EVENT_NAME = "PaymentFailedEvent";

  public PaymentFailedEvent(
      Long memberId,
      Long orderId,
      String orderNo,
      BigDecimal amount,
      PaymentErrorCode failureReason) {
    this(memberId, orderId, orderNo, amount, failureReason, EventUtils.extractTraceId());
  }

  public static PaymentFailedEvent from(
      PaymentProcessContext context, PaymentErrorCode failureReason) {
    return new PaymentFailedEvent(
        context.buyerId(),
        context.orderId(),
        context.orderNo(),
        context.totalAmount(),
        failureReason);
  }

  @Override
  public String eventName() {
    return EVENT_NAME;
  }
}
