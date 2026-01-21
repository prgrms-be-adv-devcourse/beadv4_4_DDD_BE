package com.modeunsa.shared.payment.event;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentRequest;
import com.modeunsa.boundedcontext.payment.domain.exception.PaymentErrorCode;
import com.modeunsa.global.event.EventUtils;
import com.modeunsa.global.event.TraceableEvent;
import java.math.BigDecimal;

public record PaymentFailedEvent(
    Long memberId,
    String orderNo,
    BigDecimal amount,
    PaymentErrorCode failureReason,
    String traceId)
    implements TraceableEvent {

  public PaymentFailedEvent(
      Long memberId, String orderNo, BigDecimal amount, PaymentErrorCode failureReason) {
    this(memberId, orderNo, amount, failureReason, EventUtils.extractTraceId());
  }

  public static PaymentFailedEvent from(PaymentRequest request, PaymentErrorCode failureReason) {
    return new PaymentFailedEvent(
        request.buyerId(), request.orderNo(), request.totalAmount(), failureReason);
  }
}
