package com.modeunsa.shared.product.event;

import com.modeunsa.global.event.EventUtils;
import com.modeunsa.global.event.TraceableEvent;
import com.modeunsa.shared.product.dto.ProductDto;

public record ProductCreatedEvent(ProductDto productDto, String traceId) implements TraceableEvent {

  public ProductCreatedEvent(ProductDto productDto) {
    this(productDto, EventUtils.extractTraceId());
  }

  @Override
  public String eventName() {
    return this.getClass().getSimpleName();
  }
}
