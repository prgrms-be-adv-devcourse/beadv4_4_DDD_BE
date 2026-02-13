package com.modeunsa.shared.product.dto.search;

import java.math.BigDecimal;

public record ProductSearchRequest(
    String name, String description, String category, String saleStatus, BigDecimal price) {}
