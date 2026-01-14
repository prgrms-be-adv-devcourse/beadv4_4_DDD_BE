package com.modeunsa.boundedcontext.product.domain;

import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Table(name = "product_product")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Product extends GeneratedIdAndAuditedEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id")
  @Setter
  // TODO: 판매자 생성 이후 nullable = false 추가
  private ProductMemberSeller seller;

  @Column(length = 100, nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  private ProductCategory category;

  private String description;
  @Builder.Default private BigDecimal salePrice = BigDecimal.ZERO;
  @Builder.Default private BigDecimal price = BigDecimal.ZERO;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  private ProductCurrency currency = ProductCurrency.KRW;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  private SaleStatus saleStatus = SaleStatus.NOT_SALE;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  private ProductStatus productStatus = ProductStatus.DRAFT;

  // TODO: 이벤트/트랜잭션으로 증감 관리 (정합성 전략 필요)
  @Builder.Default private int favoriteCount = 0;

  @Builder.Default private int qty = 0;

  @OneToMany(mappedBy = "product")
  @OrderBy("sortOrder ASC")
  @Builder.Default
  private List<ProductImage> images = new ArrayList<>();

  public void addImage(ProductImage image) {
    images.add(image);
    image.setProduct(this);
  }

  public void removeImage(ProductImage image) {
    images.remove(image);
    image.setProduct(null);
  }

  public void updateSaleStatus(SaleStatus saleStatus) {
    this.saleStatus = saleStatus;
  }
}
