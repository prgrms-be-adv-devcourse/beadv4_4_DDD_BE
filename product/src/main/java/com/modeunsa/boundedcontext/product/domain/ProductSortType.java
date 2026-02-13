package com.modeunsa.boundedcontext.product.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Sort;

@Getter
@AllArgsConstructor
public enum ProductSortType {
  LATEST(Sort.by(Sort.Direction.DESC, "createdAt"), "최신순"),

  PRICE_ASC(
      Sort.by(Sort.Direction.ASC, "salePrice").and(Sort.by(Sort.Direction.DESC, "createdAt")),
      "가격 낮은순"),

  PRICE_DESC(
      Sort.by(Sort.Direction.DESC, "salePrice").and(Sort.by(Sort.Direction.DESC, "createdAt")),
      "가격 높은순");

  private final Sort sort;
  private final String description;
}
