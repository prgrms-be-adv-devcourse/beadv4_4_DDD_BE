package com.modeunsa.boundedcontext.content.domain.entity;

import com.modeunsa.global.jpa.converter.EncryptedStringConverter;
import com.modeunsa.global.jpa.entity.ManualIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "content_member")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentMember extends ManualIdAndAuditedEntity {

  @Convert(converter = EncryptedStringConverter.class)
  private String email;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(nullable = false)
  private String author;

  public static ContentMember create(Long id, String email, String author) {
    ContentMember contentMember = ContentMember.builder().email(email).author(author).build();
    contentMember.assignId(id);
    return contentMember;
  }
}
