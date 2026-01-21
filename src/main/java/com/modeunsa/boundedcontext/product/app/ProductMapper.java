package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.shared.product.dto.ProductDetailResponse;
import com.modeunsa.shared.product.dto.ProductDto;
import com.modeunsa.shared.product.dto.ProductOrderDto;
import com.modeunsa.shared.product.dto.ProductOrderResponse;
import com.modeunsa.shared.product.dto.ProductResponse;
import com.modeunsa.shared.product.dto.ProductStockDto;
import com.modeunsa.shared.product.dto.ProductStockResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class ProductMapper {
  @Mapping(source = "seller.id", target = "sellerId")
  public abstract ProductResponse toResponse(Product product);

  @Mapping(source = "product.seller.id", target = "sellerId")
  @Mapping(target = "isFavorite", source = "isFavorite")
  public abstract ProductDetailResponse toDetailResponse(Product product, boolean isFavorite);

  @Mapping(source = "seller.id", target = "sellerId")
  public abstract ProductDto toDto(Product product);

  @Mapping(source = "stock", target = "stock")
  @Mapping(source = "id", target = "productId")
  @Mapping(target = "isAvailable", ignore = true)
  public abstract ProductOrderDto toProductOrderDto(Product product);

  public abstract ProductOrderResponse toProductOrderResponse(ProductOrderDto dto);

  public abstract ProductStockResponse toProductStockResponse(ProductStockDto dto);
}
