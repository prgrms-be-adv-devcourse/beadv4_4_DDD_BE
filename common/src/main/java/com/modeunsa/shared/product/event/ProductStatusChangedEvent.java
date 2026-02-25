package com.modeunsa.shared.product.event;

import com.modeunsa.global.event.EventUtils;
import com.modeunsa.global.event.TraceableEvent;
import com.modeunsa.shared.product.dto.ProductStatusDto;

public record ProductStatusChangedEvent(ProductStatusDto productStatusDto, String traceId)
    implements TraceableEvent {

  public static final String EVENT_NAME = "ProductStatusChangedEvent";

  public ProductStatusChangedEvent(ProductStatusDto productStatusDto) {
    this(productStatusDto, EventUtils.extractTraceId());
  }

  @Override
  public String eventName() {
    return this.getClass().getSimpleName();
  }
}
