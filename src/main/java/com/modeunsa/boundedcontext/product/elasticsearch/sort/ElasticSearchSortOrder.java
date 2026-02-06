package com.modeunsa.boundedcontext.product.elasticsearch.sort;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ElasticSearchSortOrder {
  ASC("오름차순"),
  DESC("내림차순");

  private final String description;
}
