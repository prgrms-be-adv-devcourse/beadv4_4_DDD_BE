package com.modeunsa.boundedcontext.payment.app.event;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentPayoutDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentPayoutCompletedEvent {
  private final PaymentPayoutDto payout;
}
