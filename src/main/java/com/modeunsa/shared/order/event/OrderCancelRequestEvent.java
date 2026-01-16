package com.modeunsa.shared.order.event;

import com.modeunsa.shared.order.dto.OrderDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderCancelRequestEvent {
  private final OrderDto orderDto;
}
