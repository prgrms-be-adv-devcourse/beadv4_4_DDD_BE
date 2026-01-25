package com.modeunsa.boundedcontext.content.out.search;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 문서 형식, 실제 데이터는 Document를 지나 Elasticsearch 문서에 저장됨
@Getter
@RequiredArgsConstructor
public class ContentSearchDocument {

  private Long contentId;
  private String text;
  //  "#봄_코디 #여름룩" → ["봄", "코디", "여름룩"]
  private List<String> tags;
  private LocalDateTime createdAt;

  public ContentSearchDocument(
      Long contentId, String text, List<String> tags, LocalDateTime createdAt) {
    this.contentId = contentId;
    this.text = text;
    this.tags = tags;
    this.createdAt = createdAt;
  }

  public Long getContentId() {
    return contentId;
  }

  public String getText() {
    return text;
  }

  public List<String> getTags() {
    return tags;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
