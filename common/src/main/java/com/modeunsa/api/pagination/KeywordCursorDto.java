package com.modeunsa.api.pagination;

public record KeywordCursorDto<T>(T createdAt, Long id) implements CursorDto {}
