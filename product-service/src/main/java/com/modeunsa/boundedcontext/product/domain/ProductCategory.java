package com.modeunsa.boundedcontext.product.domain;

import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StringUtils;

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

  private final String description;

  public static List<ProductCategory> fromDescriptionKeyword(String keyword) {
    if (!StringUtils.hasText(keyword)) {
      return List.of();
    }
    return Arrays.stream(values())
        .filter(category -> category.description.contains(keyword))
        .toList();
  }
}
