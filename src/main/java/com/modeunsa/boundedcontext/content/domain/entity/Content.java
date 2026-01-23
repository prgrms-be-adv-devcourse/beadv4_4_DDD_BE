package com.modeunsa.boundedcontext.content.domain.entity;

import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "content_author_id", nullable = false)
  private ContentMember author;

  @Column(nullable = false, length = 500)
  private String text;

  @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("id ASC")
  @Builder.Default
  private List<ContentTag> tags = new ArrayList<>();

  @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("sortOrder ASC, id ASC")
  @Builder.Default
  private List<ContentImage> images = new ArrayList<>();

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  public void updateText(String text) {
    this.text = text;
  }

  @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("createdAt ASC")
  private List<ContentComment> comments = new ArrayList<>();

  // 태그 추가, 내부 연관관계 일관되게 유지
  public void addTag(ContentTag tag) {
    tags.add(tag);
    tag.setContent(this);
  }

  // 태그 제거
  public void removeTag(ContentTag tag) {
    tags.remove(tag);
    tag.setContent(null);
  }

  // 이미지 추가
  public void addImage(ContentImage image) {
    images.add(image);
    image.setContent(this);
  }

  // 이미지 제거
  public void removeImage(ContentImage image) {
    images.remove(image);
    image.setContent(null);
  }

  public void setAuthor(ContentMember author) {
    this.author = author;
  }

  public void delete() {
    this.deletedAt = LocalDateTime.now();
  }

  public boolean isDeleted() {
    return this.deletedAt != null;
  }

  public void addComment(ContentComment comment) {
    comments.add(comment);
    comment.setContent(this);
  }

  public void removeComment(ContentComment comment) {
    comments.remove(comment);
    comment.setContent(null);
  }
}
