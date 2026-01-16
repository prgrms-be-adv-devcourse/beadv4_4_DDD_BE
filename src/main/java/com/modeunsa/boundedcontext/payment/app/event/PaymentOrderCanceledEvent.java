package com.modeunsa.boundedcontext.payment.app.event;

import com.modeunsa.shared.order.dto.OrderDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentOrderCanceledEvent {
  private final OrderDto order;
}
