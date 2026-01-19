package com.modeunsa.shared.payment.event;

import com.modeunsa.global.event.TraceableEvent;
import com.modeunsa.shared.payment.dto.PaymentDto;
import java.math.BigDecimal;

public record PaymentFailedEvent(
    PaymentDto payment, String resultCode, String msg, BigDecimal shortFailAmount, String traceId)
    implements TraceableEvent {}
