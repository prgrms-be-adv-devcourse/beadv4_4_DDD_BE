package com.modeunsa.boundedcontext.product.domain;

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
@Table(name = "product_image")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage extends GeneratedIdAndAuditedEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  private String imageUrl;
  private Boolean isPrimary; // 대표 이미지 여부
  @Builder.Default private int sortOrder = 0; // 노출순서

  public static ProductImage create(
      Product product, String imageUrl, boolean isPrimary, int sortOrder) {
    return ProductImage.builder()
        .product(product)
        .imageUrl(imageUrl)
        .isPrimary(isPrimary)
        .sortOrder(sortOrder)
        .build();
  }
}
