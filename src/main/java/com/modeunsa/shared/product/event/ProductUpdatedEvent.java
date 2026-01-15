package com.modeunsa.shared.product.event;

import com.modeunsa.shared.product.dto.ProductDto;

public record ProductUpdatedEvent(ProductDto productDto) {}
