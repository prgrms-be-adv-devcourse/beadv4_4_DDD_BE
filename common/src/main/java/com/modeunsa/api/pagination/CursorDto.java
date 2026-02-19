package com.modeunsa.api.pagination;

import java.time.LocalDateTime;

public record CursorDto(LocalDateTime createdAt, Long id) {}
