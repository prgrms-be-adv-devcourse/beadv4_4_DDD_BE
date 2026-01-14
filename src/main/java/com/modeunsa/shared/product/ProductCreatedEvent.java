package com.modeunsa.shared.product;

import com.modeunsa.shared.product.dto.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductCreatedEvent {

  private final ProductResponse productResponse;
}
