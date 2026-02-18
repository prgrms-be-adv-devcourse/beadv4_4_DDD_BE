package com.modeunsa.boundedcontext.product.in.dto;

import org.springframework.data.domain.Slice;

public record ProductSliceResultDto(Slice<ProductResponse> contents, String cursor) {}
