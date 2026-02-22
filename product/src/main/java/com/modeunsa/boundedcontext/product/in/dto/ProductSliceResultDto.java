package com.modeunsa.boundedcontext.product.in.dto;

import org.springframework.data.domain.Slice;

public record ProductSliceResultDto<T>(Slice<T> contents, String cursor) {}
