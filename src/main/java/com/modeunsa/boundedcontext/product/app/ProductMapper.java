package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductFavorite;
import com.modeunsa.boundedcontext.product.domain.ProductImage;
import com.modeunsa.shared.product.dto.ProductDetailResponse;
import com.modeunsa.shared.product.dto.ProductDto;
import com.modeunsa.shared.product.dto.ProductFavoriteResponse;
import com.modeunsa.shared.product.dto.ProductOrderDto;
import com.modeunsa.shared.product.dto.ProductOrderResponse;
import com.modeunsa.shared.product.dto.ProductResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public abstract class ProductMapper {
  @Mapping(source = "seller.id", target = "sellerId")
  @Mapping(source = "seller.businessName", target = "sellerBusinessName")
  @Mapping(source = "images", target = "primaryImageUrl", qualifiedByName = "primaryImageUrl")
  public abstract ProductResponse toResponse(Product product);

  @Mapping(source = "product.seller.id", target = "sellerId")
  @Mapping(source = "product.seller.businessName", target = "sellerBusinessName")
  @Mapping(target = "isFavorite", source = "isFavorite")
  public abstract ProductDetailResponse toDetailResponse(Product product, boolean isFavorite);

  @Mapping(source = "seller.id", target = "sellerId")
  @Mapping(source = "seller.businessName", target = "sellerBusinessName")
  @Mapping(source = "images", target = "primaryImageUrl", qualifiedByName = "primaryImageUrl")
  public abstract ProductDto toDto(Product product);

  @Mapping(source = "id", target = "productId")
  @Mapping(source = "seller.id", target = "sellerId")
  public abstract ProductOrderDto toProductOrderDto(Product product);

  public abstract ProductOrderResponse toProductOrderResponse(ProductOrderDto dto);

  @Mapping(source = "member.id", target = "memberId")
  @Mapping(source = "product.id", target = "productId")
  @Mapping(source = "product.name", target = "productName")
  @Mapping(source = "product.salePrice", target = "salePrice")
  @Mapping(source = "product.seller.businessName", target = "sellerBusinessName")
  @Mapping(
      source = "product.images",
      target = "primaryImageUrl",
      qualifiedByName = "primaryImageUrl")
  public abstract ProductFavoriteResponse toProductFavoriteResponse(
      ProductFavorite productFavorite);

  @Named("primaryImageUrl")
  String getPrimaryImageUrl(List<ProductImage> images) {
    if (images == null || images.isEmpty()) {
      return null;
    }
    return images.get(0).getImageUrl();
  }
}
