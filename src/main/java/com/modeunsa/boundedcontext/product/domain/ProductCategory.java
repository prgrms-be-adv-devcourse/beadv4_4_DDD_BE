package com.modeunsa.boundedcontext.product.domain;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductCategory {
  OUTER("아우터"),
  UPPER("상의"),
  LOWER("하의"),
  CAP("모자"),
  SHOES("신발"),
  BAG("가방"),
  BEAUTY("뷰티");

  @Column(name="\"value\"")
  private final String value;
}
