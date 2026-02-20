package com.modeunsa.shared.order.event;

import com.modeunsa.shared.order.dto.OrderItemDto;
import java.util.List;

public record OrderCancellationConfirmedEvent(Long id, List<OrderItemDto> orderDto) {}
