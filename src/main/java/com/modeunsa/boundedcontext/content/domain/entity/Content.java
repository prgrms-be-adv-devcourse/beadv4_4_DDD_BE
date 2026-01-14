package com.modeunsa.boundedcontext.content.domain.entity;

import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "content_content")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Content extends GeneratedIdAndAuditedEntity {

  private Long authorUserId; // 작성자 ID

  @Column(nullable = false, length = 500)
  private String text;

  @Builder.Default
  @OneToMany(
    mappedBy = "content",
    fetch = FetchType.LAZY,
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  @OrderBy("id ASC")
  private List<ContentTag> tags = new ArrayList<>();

  @Builder.Default
  @OneToMany(
    mappedBy = "content",
    fetch = FetchType.LAZY,
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  @OrderBy("sortOrder ASC, id ASC")
  private List<ContentImage> images = new ArrayList<>();

  private LocalDateTime deletedAt;

  // 생성
  public static Content create(Long authorUserId, String text) {
    return Content.builder()
      .authorUserId(authorUserId)
      .text(text)
      .build();
  }

  // 수정
  public void update(Long requesterId, String newText) {
    if (!isAuthor(requesterId)) {
      return;
    }
    this.text = newText;
  }

  // 삭제
  public void delete(Long requesterId) {
    if (!isAuthor(requesterId)) {
      return;
    }
    this.deletedAt = LocalDateTime.now();
  }

  public boolean isDeleted() {
    return this.deletedAt != null;
  }

  public boolean isAuthor(Long requesterId) {
    return requesterId != null && requesterId.equals(this.authorUserId);
  }

  public void clearTags() {
    this.tags.clear();
  }

  public void clearImages() {
    this.images.clear();
  }

  public void addTag(ContentTag tag) {
    tag.setContent(this);
    this.tags.add(tag);
  }

  public void addImage(ContentImage image) {
    image.setContent(this);
    this.images.add(image);
  }
}