package com.modeunsa.shared.payment.event;

import com.modeunsa.shared.payment.dto.PaymentDto;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentFailedEvent {
  private final PaymentDto payment;
  private final String resultCode;
  private final String msg;
  private final BigDecimal shortFailAmount;
}
