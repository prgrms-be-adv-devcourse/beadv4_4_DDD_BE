package com.modeunsa.boundedcontext.product.elasticsearch.sort;

import lombok.Getter;

@Getter
public class ElasticSearchSort {

  private final String field;
  private final ElasticSearchSortOrder order;

  private ElasticSearchSort(String field, ElasticSearchSortOrder order) {
    this.field = field;
    this.order = order; // asc, desc
  }

  public static ElasticSearchSort asc(String field) {
    return new ElasticSearchSort(field, ElasticSearchSortOrder.ASC);
  }

  public static ElasticSearchSort desc(String field) {
    return new ElasticSearchSort(field, ElasticSearchSortOrder.DESC);
  }

  public String getField() {
    return field;
  }

  public ElasticSearchSortOrder getOrder() {
    return order;
  }
}
