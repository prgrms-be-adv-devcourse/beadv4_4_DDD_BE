package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductMemberSeller;
import com.modeunsa.shared.product.dto.ProductDto;
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
  public abstract Product toEntity(ProductDto productDto);

  @AfterMapping
  protected void setSeller(ProductDto dto, @MappingTarget Product product) {
    if (dto.getSellerId() != 0) {
      ProductMemberSeller seller =
          entityManager.getReference(ProductMemberSeller.class, dto.getSellerId());
      product.setSeller(seller);
    }
  }
}
