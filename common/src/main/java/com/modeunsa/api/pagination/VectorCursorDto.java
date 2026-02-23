package com.modeunsa.api.pagination;

public record VectorCursorDto(double score, String id) implements CursorDto {}
