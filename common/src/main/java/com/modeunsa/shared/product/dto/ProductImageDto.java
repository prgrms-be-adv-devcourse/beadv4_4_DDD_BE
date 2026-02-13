package com.modeunsa.shared.product.dto;

public record ProductImageDto(Long id, String imageUrl, Boolean isPrimary, int sortOrder) {}
