package com.modeunsa.shared.product.event;

import com.modeunsa.shared.product.dto.ProductResponse;

public record ProductUpdatedEvent(ProductResponse productResponse) {}
