package com.modeunsa.boundedcontext.content.out.search;

import lombok.Getter;

// 검색 요청 값만 들고 있음
@Getter
public class ContentSearchCondition {

  private final String keyword;
  private final int page;
  private final int size;

  public ContentSearchCondition(String keyword, int page, int size) {
    this.keyword = keyword;
    this.page = page;
    this.size = size;
  }

  public String getKeyword() {
    return keyword;
  }

  public int getPage() {
    return page;
  }

  public int getSize() {
    return size;
  }
}
