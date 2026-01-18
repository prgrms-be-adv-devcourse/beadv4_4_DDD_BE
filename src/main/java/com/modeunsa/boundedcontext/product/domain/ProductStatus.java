package com.modeunsa.boundedcontext.product.domain;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductStatus {
  DRAFT("임시저장"),
  COMPLETED("완료"),
  CANCELED("취소");

  @Column(name="\"value\"")
  private final String value;
}
