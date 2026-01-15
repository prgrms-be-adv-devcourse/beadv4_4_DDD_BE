package com.modeunsa.boundedcontext.product.domain;

import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import com.modeunsa.shared.product.dto.ProductCreateRequest;
import com.modeunsa.shared.product.dto.ProductUpdateRequest;
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
  @JoinColumn(name = "seller_id", nullable = false)
  @Setter
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

  @Builder.Default private int quantity = 0;

  @OneToMany(mappedBy = "product")
  @OrderBy("sortOrder ASC")
  @Builder.Default
  private List<ProductImage> images = new ArrayList<>();

  public static Product create(ProductMemberSeller seller, ProductCreateRequest request) {
    return Product.builder()
        .seller(seller)
        .name(request.getName())
        .category(request.getCategory())
        .description(request.getDescription())
        .currency(ProductCurrency.KRW)
        .saleStatus(SaleStatus.NOT_SALE)
        .productStatus(request.getProductStatus())
        .favoriteCount(0)
        .quantity(request.getQuantity())
        .build();
  }

  public void update(ProductUpdateRequest request) {
    if (request.getName() != null) {
      this.name = request.getName();
    }
    if (request.getCategory() != null) {
      this.category = request.getCategory();
    }
    if (request.getDescription() != null) {
      this.description = request.getDescription();
    }
    if (request.getSaleStatus() != null) {
      this.saleStatus = request.getSaleStatus();
    }
    if (request.getPrice() != null) {
      this.price = request.getPrice();
    }
    if (request.getSalePrice() != null) {
      this.salePrice = request.getSalePrice();
    }
    if (request.getQuantity() != null) {
      this.quantity = request.getQuantity();
    }
    // TODO: image 수정 추가
  }

  public void addImage(ProductImage image) {
    images.add(image);
    image.setProduct(this);
  }

  public void removeImage(ProductImage image) {
    images.remove(image);
    image.setProduct(null);
  }
}
