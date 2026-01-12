package com.modeunsa.boundedcontext.product.domain;

import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(name = "product_member")
@Getter
@Builder
public class ProductMember extends GeneratedIdAndAuditedEntity {
  // TODO: member 생성 이후 추가 예정
  private long memberId;
  private String name;
}
