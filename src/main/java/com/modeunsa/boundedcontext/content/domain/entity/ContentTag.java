package com.modeunsa.boundedcontext.content.domain.entity;

import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import com.modeunsa.global.status.ErrorStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "content_tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentTag extends GeneratedIdAndAuditedEntity {

  private static final int MAX_LENGTH = 10;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "content_id", nullable = false)
  private Content content;

  @Column(name="\"value\"", length = MAX_LENGTH, nullable = false)
  private String value;

  public ContentTag(String value) {
    validate(value);
    this.value = value.trim();
  }

  void setContent(Content content) {
    this.content = content;
  }

  private void validate(String value) {
    if (value == null || value.isBlank()) {
      throw new GeneralException(ErrorStatus.CONTENT_TAG_REQUIRED);
    }

    if (value.length() > MAX_LENGTH) {
      throw new GeneralException(ErrorStatus.CONTENT_TAG_SIZE_EXCEEDED);
    }
  }
}
