package com.modeunsa.shared.inventory.event;

import com.modeunsa.global.event.EventUtils;
import com.modeunsa.global.event.TraceableEvent;
import com.modeunsa.shared.order.dto.OrderItemDto;
import java.util.List;

public record InventoryStockRecoverEvent(List<OrderItemDto> orderItems, String traceId)
    implements TraceableEvent {
  public static final String EVENT_NAME = "InventoryStockRecoverEvent";

  public InventoryStockRecoverEvent(List<OrderItemDto> orderItems) {
    this(orderItems, EventUtils.extractTraceId());
  }

  @Override
  public String eventName() {
    return EVENT_NAME;
  }
}
