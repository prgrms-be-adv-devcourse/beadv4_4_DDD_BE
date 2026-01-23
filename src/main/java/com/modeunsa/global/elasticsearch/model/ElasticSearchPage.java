package com.modeunsa.global.elasticsearch.model;

import java.util.Collections;
import java.util.List;

public class ElasticSearchPage<T> {

  // 검색 결과에 맞는 전체 문서 개수
  private final long total;
  // 현재 페이지에 포함된 검색 결과 목록
  private final List<ElasticSearchHit<T>> hits;
  private final int page;
  // 한 페이지당 결과 개수
  private final int size;

  public ElasticSearchPage(long total, List<ElasticSearchHit<T>> hits, int page, int size) {
    this.total = total;
    this.hits = hits == null ? Collections.emptyList() : hits;
    this.page = page;
    this.size = size;
  }

  public long getTotal() {
    return total;
  }

  public List<ElasticSearchHit<T>> getHits() {
    return hits;
  }

  public int getPage() {
    return page;
  }

  public int getSize() {
    return size;
  }
}
