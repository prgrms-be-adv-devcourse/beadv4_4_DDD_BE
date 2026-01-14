package com.modeunsa.boundedcontext.content.domain.entity;

import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;

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
@Table(name = "content_image")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentImage extends GeneratedIdAndAuditedEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "content_id", nullable = false)
  private Content content;

  private String imageUrl;
  private Boolean isPrimary;  //노출 순서
  @Builder.Default
  private int sortOrder = 0;

  public void setProduct(Content content) {
    this.content = content;
  }
}
