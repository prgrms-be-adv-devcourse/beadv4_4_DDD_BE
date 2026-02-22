package com.modeunsa.shared.product.event;

import com.modeunsa.global.event.EventUtils;
import com.modeunsa.global.event.TraceableEvent;
import com.modeunsa.shared.product.dto.ProductOrderAvailableDto;

public record ProductOrderAvailabilityChangedEvent(
    ProductOrderAvailableDto productOrderAvailableDto, String traceId) implements TraceableEvent {

  public ProductOrderAvailabilityChangedEvent(ProductOrderAvailableDto productOrderAvailableDto) {
    this(productOrderAvailableDto, EventUtils.extractTraceId());
  }

  @Override
  public String eventName() {
    return this.getClass().getSimpleName();
  }
}
