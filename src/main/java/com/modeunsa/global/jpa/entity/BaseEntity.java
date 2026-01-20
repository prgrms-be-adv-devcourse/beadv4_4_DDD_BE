package com.modeunsa.global.jpa.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public interface BaseEntity {

  Long getId();

  LocalDateTime getCreatedAt();

  LocalDateTime getUpdatedAt();

  Long getCreatedBy();

  Long getUpdatedBy();
}
