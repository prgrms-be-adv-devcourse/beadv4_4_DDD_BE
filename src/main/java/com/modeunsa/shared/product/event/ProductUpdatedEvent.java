package com.modeunsa.shared.product.event;

import com.modeunsa.shared.product.dto.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductUpdatedEvent {

  private final ProductResponse productResponse;
}
