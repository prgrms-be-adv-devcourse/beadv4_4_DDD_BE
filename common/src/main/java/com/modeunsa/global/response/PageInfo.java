package com.modeunsa.global.response;

public record PageInfo(
    Integer page, Integer size, Boolean hasNext, Long totalElements, Integer totalPages)
    implements PaginationInfo {}
