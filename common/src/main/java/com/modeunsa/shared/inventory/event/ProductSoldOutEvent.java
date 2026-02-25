package com.modeunsa.shared.inventory.event;

import com.modeunsa.global.event.EventUtils;
import com.modeunsa.global.event.TraceableEvent;

public record ProductSoldOutEvent(Long productId, String traceId) implements TraceableEvent {

  public static final String EVENT_NAME = "ProductSoldOutEvent";

  public ProductSoldOutEvent(Long productId) {
    this(productId, EventUtils.extractTraceId());
  }

  @Override
  public String eventName() {
    return EVENT_NAME;
  }
}
