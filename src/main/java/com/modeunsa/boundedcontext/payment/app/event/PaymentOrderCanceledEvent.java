package com.modeunsa.boundedcontext.payment.app.event;

import com.modeunsa.global.event.TraceableEvent;
import com.modeunsa.shared.order.dto.OrderDto;

public record PaymentOrderCanceledEvent(OrderDto order, String traceId) implements TraceableEvent {}
