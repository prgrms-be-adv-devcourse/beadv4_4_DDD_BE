package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.shared.product.dto.ProductDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
  @Mapping(target = "productStatus", defaultValue = "DRAFT")
  @Mapping(target = "saleStatus", defaultValue = "NOT_SALE")
  @Mapping(target = "currency", defaultValue = "KRW")
  Product toEntity(ProductDto productDto);
}
