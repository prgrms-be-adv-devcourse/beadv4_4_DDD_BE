package com.modeunsa.boundedcontext.product.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductStatus {
  DRAFT("임시저장"),
  COMPLETED("완료"),
  CANCELED("취소");

  private final String description;
}
