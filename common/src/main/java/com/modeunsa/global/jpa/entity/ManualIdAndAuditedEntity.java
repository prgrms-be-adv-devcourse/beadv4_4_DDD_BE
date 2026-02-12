package com.modeunsa.global.jpa.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@SuperBuilder
@NoArgsConstructor
public abstract class ManualIdAndAuditedEntity {

  @Id private Long id;
  @CreatedDate private LocalDateTime createdAt;
  @LastModifiedDate private LocalDateTime updatedAt;
  @CreatedBy private Long createdBy; // member.id
  @LastModifiedBy private Long updatedBy; // member.id

  protected void assignId(Long id) {
    this.id = id;
  }
}
