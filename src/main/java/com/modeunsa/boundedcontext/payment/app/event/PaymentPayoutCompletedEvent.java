package com.modeunsa.boundedcontext.payment.app.event;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentPayoutDto;
import com.modeunsa.global.event.TraceableEvent;

public record PaymentPayoutCompletedEvent(PaymentPayoutDto payout, String traceId)
    implements TraceableEvent {}
