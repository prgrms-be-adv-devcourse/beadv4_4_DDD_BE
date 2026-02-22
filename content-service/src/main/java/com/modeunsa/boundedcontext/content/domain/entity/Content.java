package com.modeunsa.boundedcontext.content.domain.entity;

import com.modeunsa.boundedcontext.content.app.dto.image.ContentImageDto;
import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @JoinColumn(name = "content_author_id", nullable = false)
  private ContentMember author;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, length = 500)
  private String text;

  @OneToMany(
      mappedBy = "content",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
      orphanRemoval = true)
  @Builder.Default
  private List<ContentTag> tags = new ArrayList<>();

  private String mainImageUrl;

  @OneToMany(
      mappedBy = "content",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
      orphanRemoval = true)
  @Builder.Default
  private List<ContentImage> images = new ArrayList<>();

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  private int likeCount;

  private int commentCount;

  public void updateText(String text) {
    this.text = text;
  }

  public void updateMainImageUrl(String url) {
    this.mainImageUrl = url;
  }

  @OneToMany(
      mappedBy = "content",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE},
      orphanRemoval = true)
  @Builder.Default
  private List<ContentComment> comments = new ArrayList<>();

  public static Content create(
      ContentMember author,
      String title,
      String text,
      List<String> tags,
      List<ContentImageDto> images) {
    Content content = Content.builder().title(title).author(author).text(text).build();

    for (String tagValue : tags) {
      content.addTag(new ContentTag(tagValue));
    }

    for (ContentImageDto spec : images) {
      ContentImage image =
          new ContentImage(
              spec.imageUrl(), spec.isPrimary(), spec.sortOrder() != null ? spec.sortOrder() : 0);
      if (Boolean.TRUE.equals(spec.isPrimary())) {
        content.mainImageUrl = spec.imageUrl();
      }
      content.addImage(image);
    }

    return content;
  }

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
