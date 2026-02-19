package com.modeunsa.shared.payment.event;

import com.modeunsa.global.event.EventUtils;
import com.modeunsa.global.event.TraceableEvent;
import java.math.BigDecimal;

public record PaymentFailedEvent(
    Long memberId,
    Long orderId,
    String orderNo,
    BigDecimal amount,
    String errorCode,
    String failureMessage,
    String traceId)
    implements TraceableEvent {

  public static final String EVENT_NAME = "PaymentFailedEvent";

  public PaymentFailedEvent(
      Long memberId,
      Long orderId,
      String orderNo,
      BigDecimal amount,
      String errorCode,
      String failureMessage) {
    this(
        memberId, orderId, orderNo, amount, errorCode, failureMessage, EventUtils.extractTraceId());
  }

  public static PaymentFailedEvent from(
      Long buyerId,
      Long orderId,
      String orderNo,
      BigDecimal totalAmount,
      String errorCode,
      String failureMessage) {
    return new PaymentFailedEvent(
        buyerId, orderId, orderNo, totalAmount, errorCode, failureMessage);
  }

  @Override
  public String eventName() {
    return EVENT_NAME;
  }
}
