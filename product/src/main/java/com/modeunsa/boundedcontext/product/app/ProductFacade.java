package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.api.pagination.CursorCodec;
import com.modeunsa.api.pagination.CursorDto;
import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.boundedcontext.product.domain.ProductFavorite;
import com.modeunsa.boundedcontext.product.domain.ProductMember;
import com.modeunsa.boundedcontext.product.domain.ProductStatus;
import com.modeunsa.boundedcontext.product.domain.SaleStatus;
import com.modeunsa.boundedcontext.product.in.dto.ProductCreateRequest;
import com.modeunsa.boundedcontext.product.in.dto.ProductDetailResponse;
import com.modeunsa.boundedcontext.product.in.dto.ProductResponse;
import com.modeunsa.boundedcontext.product.in.dto.ProductSliceResultDto;
import com.modeunsa.boundedcontext.product.in.dto.ProductUpdateRequest;
import com.modeunsa.shared.product.dto.ProductFavoriteResponse;
import com.modeunsa.shared.product.dto.ProductOrderResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductFacade {

  private final ProductCreateProductUseCase productCreateProductUseCase;
  private final ProductUpdateProductUseCase productUpdateProductUseCase;
  private final ProductUpdateProductStatusUseCase productUpdateProductStatusUseCase;
  private final ProductCreateFavoriteUseCase productCreateFavoriteUseCase;
  private final ProductDeleteFavoriteUseCase productDeleteFavoriteUseCase;
  private final ProductValidateOrderUseCase productValidateOrderUseCase;
  private final ProductCreateMemberUseCase productCreateMemberUseCase;
  private final ProductCreateSellerUseCase productCreateSellerUseCase;
  private final ProductUpdateMemberUseCase productUpdateMemberUseCase;
  private final ProductSupport productSupport;
  private final ProductMapper productMapper;
  private final CursorCodec cursorCodec;

  @Transactional
  public ProductDetailResponse createProduct(
      Long sellerId, ProductCreateRequest productCreateRequest) {
    Product product = productCreateProductUseCase.createProduct(sellerId, productCreateRequest);
    return productMapper.toDetailResponse(product, false);
  }

  public ProductDetailResponse getProduct(Long memberId, Long productId) {
    Product product = productSupport.getProduct(productId);
    boolean isFavorite;
    if (memberId == null) {
      isFavorite = false;
    } else {
      ProductMember member = productSupport.getProductMember(memberId);
      isFavorite = productSupport.existsProductFavorite(member.getId(), product.getId());
    }
    return productMapper.toDetailResponse(product, isFavorite);
  }

  public Page<ProductResponse> getProducts(ProductCategory category, Pageable pageable) {
    Page<Product> products = productSupport.getProducts(category, pageable);
    return products.map(product -> productMapper.toResponse(product));
  }

  public Page<ProductResponse> getProducts(
      Long sellerId,
      String name,
      ProductCategory category,
      SaleStatus saleStatus,
      ProductStatus productStatus,
      Pageable pageable) {
    Page<Product> products =
        productSupport.getProducts(sellerId, name, category, saleStatus, productStatus, pageable);
    return products.map(product -> productMapper.toResponse(product));
  }

  public ProductSliceResultDto getProducts(String keyword, String cursor, int size) {
    // 1. cursor 복호화
    CursorDto decodedCursor = cursorCodec.decodeIfPresent(cursor);
    // 2. cursor 기반 검색
    Slice<Product> products = productSupport.getProducts(keyword, decodedCursor, size);
    // 3. nextCursor 가져와서 암호화 & 인코딩
    String nextCursor = null;
    if (products.hasNext()) {
      Product last = products.getContent().getLast();
      nextCursor = cursorCodec.encode(new CursorDto(last.getCreatedAt(), last.getId()));
    }

    return new ProductSliceResultDto(
        products.map(product -> productMapper.toResponse(product)), nextCursor);
  }

  public List<ProductOrderResponse> getProducts(List<Long> productIds) {
    return productValidateOrderUseCase.validateOrder(productIds).stream()
        .map(productMapper::toProductOrderResponse)
        .toList();
  }

  @Transactional
  public ProductDetailResponse updateProduct(
      Long memberId, Long sellerId, Long productId, ProductUpdateRequest productRequest) {
    Product product =
        productUpdateProductUseCase.updateProduct(sellerId, productId, productRequest);
    ProductMember member = productSupport.getProductMember(memberId);
    boolean isFavorite = productSupport.existsProductFavorite(member.getId(), product.getId());
    return productMapper.toDetailResponse(product, isFavorite);
  }

  @Transactional
  public ProductDetailResponse updateProductStatus(
      Long memberId, Long sellerId, Long productId, ProductStatus productStatus) {
    Product product =
        productUpdateProductStatusUseCase.updateProductStatus(sellerId, productId, productStatus);
    ProductMember member = productSupport.getProductMember(memberId);
    boolean isFavorite = productSupport.existsProductFavorite(member.getId(), product.getId());
    return productMapper.toDetailResponse(product, isFavorite);
  }

  @Transactional
  public void createProductFavorite(Long memberId, Long productId) {
    productCreateFavoriteUseCase.createProductFavorite(memberId, productId);
  }

  @Transactional
  public void deleteProductFavorite(Long memberId, Long productId) {
    productDeleteFavoriteUseCase.deleteProductFavorite(memberId, productId);
  }

  public Page<ProductFavoriteResponse> getProductFavorites(Long memberId, Pageable pageable) {
    Page<ProductFavorite> productFavorites = productSupport.getProductFavorites(memberId, pageable);
    return productFavorites.map(productMapper::toProductFavoriteResponse);
  }

  public void syncMember(Long memberId, String email, String name, String phoneNumber) {
    productCreateMemberUseCase.createMember(memberId, email, name, phoneNumber);
  }

  public void syncSeller(
      Long sellerId, Long memberId, String businessName, String representativeName) {
    productCreateSellerUseCase.createMemberSeller(
        sellerId, memberId, businessName, representativeName);
  }

  public void updateMember(Long memberId, String realName, String email, String phoneNumber) {
    productUpdateMemberUseCase.updateMember(memberId, realName, email, phoneNumber);
  }
}
