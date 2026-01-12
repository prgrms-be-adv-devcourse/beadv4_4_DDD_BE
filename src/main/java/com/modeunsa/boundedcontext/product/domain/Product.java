package com.modeunsa.boundedcontext.product.domain;

import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Table(name = "product_product")
@Builder
public class Product extends GeneratedIdAndAuditedEntity {
  private long sellerId; // 판매자 id
  private String name;

  @Enumerated(EnumType.STRING)
  private ProductCategory category;

  private String description;
  @Builder.Default private int salePrice = 0;
  @Builder.Default private int price = 0;

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
  private List<ProductImage> images;

  public void addImage(ProductImage image) {
    images.add(image);
    image.setProduct(this);
  }

  public void removeImage(ProductImage image) {
    images.remove(image);
    image.setProduct(null);
  }
}
