package com.modeunsa.shared.order.event;

import com.modeunsa.shared.order.dto.OrderDto;

public record OrderPaidEvent(OrderDto orderDto) {}
