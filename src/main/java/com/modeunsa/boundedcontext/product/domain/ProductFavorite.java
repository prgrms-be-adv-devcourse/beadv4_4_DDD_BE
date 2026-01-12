package com.modeunsa.boundedcontext.product.domain;

import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(
    name = "product_favorite",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_product_favorite_member_product",
            columnNames = {"member_id", "product_id"}))
@Getter
@Builder
public class ProductFavorite extends GeneratedIdAndAuditedEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  private ProductMember member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;
}
