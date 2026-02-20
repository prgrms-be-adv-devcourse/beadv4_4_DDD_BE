package com.modeunsa.shared.product.event;

import com.modeunsa.global.event.EventUtils;
import com.modeunsa.global.event.TraceableEvent;
import com.modeunsa.shared.product.dto.ProductDto;
import java.util.Set;

public record ProductUpdatedEvent(ProductDto productDto, Set<String> changedFields, String traceId)
    implements TraceableEvent {

  public ProductUpdatedEvent(ProductDto productDto, Set<String> changedFields) {
    this(productDto, changedFields, EventUtils.extractTraceId());
  }

  @Override
  public String eventName() {
    return this.getClass().getSimpleName();
  }
}
