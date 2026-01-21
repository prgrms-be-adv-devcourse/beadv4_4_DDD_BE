package com.modeunsa.global.elasticsearch;

public enum IndexName {
  CONTENT("content_index_v1"),
  PRODUCT("product_index_v1");

  private final String description;

  IndexName(String description) {
    this.description = description;
  }

  public String description() {
    return description;
  }
}
