package com.modeunsa.shared.payment.event;

import com.modeunsa.shared.payment.dto.PaymentDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentSuccessEvent {
  private final PaymentDto payment;
}
