package com.modeunsa.shared.order.event;

import com.modeunsa.shared.order.dto.OrderDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OrderCreatedEvent {
  public final OrderDto orderDto;
}
