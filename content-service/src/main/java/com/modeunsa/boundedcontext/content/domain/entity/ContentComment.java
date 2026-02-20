package com.modeunsa.boundedcontext.content.domain.entity;

import static com.modeunsa.global.status.ErrorStatus.CONTENT_COMMENT_LENGTH_EXCEEDED;

import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "content_comment")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class ContentComment extends GeneratedIdAndAuditedEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "content_id", nullable = false)
  private Content content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "content_author_id", nullable = false)
  private ContentMember author;

  @Column(nullable = false, length = 100)
  private String text;

  public static ContentComment createComment(Content content, ContentMember author, String text) {
    if (text.length() > 100) {
      throw new GeneralException(CONTENT_COMMENT_LENGTH_EXCEEDED);
    }

    return ContentComment.builder().content(content).author(author).text(text).build();
  }

  void setContent(Content content) {
    this.content = content;
  }
}
