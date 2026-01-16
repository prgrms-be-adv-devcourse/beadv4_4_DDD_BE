package com.modeunsa.boundedcontext.product.domain;

import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_member_seller")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProductMemberSeller extends GeneratedIdAndAuditedEntity {
  @Column(nullable = false, length = 200)
  private String businessName;

  @Column(nullable = false, length = 100)
  private String representativeName;
}
