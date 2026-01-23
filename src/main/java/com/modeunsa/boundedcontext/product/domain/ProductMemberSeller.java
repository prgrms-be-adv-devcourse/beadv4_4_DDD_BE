package com.modeunsa.boundedcontext.product.domain;

import com.modeunsa.global.jpa.entity.ManualIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_member_seller")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProductMemberSeller extends ManualIdAndAuditedEntity {
  @Column(nullable = false, length = 200)
  private String businessName;

  @Column(nullable = false, length = 100)
  private String representativeName;

  public static ProductMemberSeller create(
      Long sellerId, String businessName, String representativeName) {
    ProductMemberSeller seller =
        ProductMemberSeller.builder()
            .businessName(businessName)
            .representativeName(representativeName)
            .build();
    seller.assignId(sellerId);
    return seller;
  }
}
