package com.modeunsa.shared.content.dto;

import java.time.LocalDateTime;

public record ContentDto(
    Long id,
    Long name,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long createdBy,
    Long updatedBy,
    String primaryImageUrl,
    String tag,
    String text) {}
