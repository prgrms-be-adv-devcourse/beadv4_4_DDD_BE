package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductMemberSeller;
import com.modeunsa.shared.product.dto.ProductRequest;
import com.modeunsa.shared.product.dto.ProductResponse;
import jakarta.persistence.EntityManager;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class ProductMapper {
  @Autowired protected EntityManager entityManager;

  @Mapping(target = "productStatus", defaultValue = "DRAFT")
  @Mapping(target = "saleStatus", defaultValue = "NOT_SALE")
  @Mapping(target = "currency", defaultValue = "KRW")
  @Mapping(target = "images", ignore = true)
  @Mapping(target = "favoriteCount", ignore = true)
  @Mapping(target = "seller", ignore = true)
  public abstract Product toEntity(ProductRequest productRequest);

  @Mapping(source = "seller.id", target = "sellerId")
  public abstract ProductResponse toResponse(Product product);

  @AfterMapping
  protected void setSeller(ProductRequest productRequest, @MappingTarget Product product) {
    if (productRequest.getSellerId() != 0) {
      ProductMemberSeller seller =
          entityManager.getReference(ProductMemberSeller.class, productRequest.getSellerId());
      product.setSeller(seller);
    }
  }
}
