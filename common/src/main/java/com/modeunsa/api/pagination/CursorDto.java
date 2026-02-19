package com.modeunsa.api.pagination;

public record CursorDto<T>(T createdAt, Long id) {}
