package com.modeunsa.global.elasticsearch.model;

public class ElasticSearchHit<T> {

  private final String id;
  private final double score;
  private final T source;

  // 검색 결과 한 건(hit)
  public ElasticSearchHit(String id, double score, T source) {
    // 문서id
    this.id = id;
    /* 관련도 점수 (기본 정렬 관련)
    TODO: 나중에 기본 정렬 기능 구현
     */
    this.score = score;
    // 검색된 데이터
    this.source = source;
  }

  public String getId() {
    return id;
  }

  public double getScore() {
    return score;
  }

  public T getSource() {
    return source;
  }
}
