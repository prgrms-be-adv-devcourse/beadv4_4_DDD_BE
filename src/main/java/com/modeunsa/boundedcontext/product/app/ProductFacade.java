package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.ProductCreateMemberUseCase;
import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.boundedcontext.product.domain.ProductMember;
import com.modeunsa.boundedcontext.product.domain.ProductStatus;
import com.modeunsa.shared.order.dto.OrderDto;
import com.modeunsa.shared.product.dto.ProductCreateRequest;
import com.modeunsa.shared.product.dto.ProductDetailResponse;
import com.modeunsa.shared.product.dto.ProductOrderResponse;
import com.modeunsa.shared.product.dto.ProductResponse;
import com.modeunsa.shared.product.dto.ProductStockResponse;
import com.modeunsa.shared.product.dto.ProductStockUpdateRequest;
import com.modeunsa.shared.product.dto.ProductUpdateRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
  private final ProductDeductStockUseCase productDeductStockUseCase;
  private final ProductRestoreStockUseCase productRestoreStockUseCase;
  private final ProductCreateMemberUseCase productCreateMemberUseCase;
  private final ProductCreateSellerUseCase productCreateSellerUseCase;
  private final ProductSupport productSupport;
  private final ProductMapper productMapper;

  @Transactional
  public ProductResponse createProduct(Long sellerId, ProductCreateRequest productCreateRequest) {
    Product product = productCreateProductUseCase.createProduct(sellerId, productCreateRequest);
    return productMapper.toResponse(product);
  }

  public ProductDetailResponse getProduct(Long memberId, Long productId) {
    Product product = productSupport.getProduct(productId);
    ProductMember member = productSupport.getProductMember(memberId);
    boolean isFavorite = productSupport.existsProductFavorite(member.getId(), product.getId());
    return productMapper.toDetailResponse(product, isFavorite);
  }

  public Page<ProductResponse> getProducts(
      Long memberId, ProductCategory category, Pageable pageable) {
    Page<Product> products = productSupport.getProducts(memberId, category, pageable);
    return products.map(productMapper::toResponse);
  }

  public List<ProductOrderResponse> getProducts(List<Long> productIds) {
    return productValidateOrderUseCase.validateOrder(productIds).stream()
        .map(productMapper::toProductOrderResponse)
        .toList();
  }

  @Transactional
  public ProductResponse updateProduct(
      Long sellerId, Long productId, ProductUpdateRequest productRequest) {
    Product product =
        productUpdateProductUseCase.updateProduct(sellerId, productId, productRequest);
    return productMapper.toResponse(product);
  }

  @Transactional
  public ProductResponse updateProductStatus(
      Long sellerId, Long productId, ProductStatus productStatus) {
    Product product =
        productUpdateProductStatusUseCase.updateProductStatus(sellerId, productId, productStatus);
    return productMapper.toResponse(product);
  }

  @Transactional
  public void createProductFavorite(Long memberId, Long productId) {
    productCreateFavoriteUseCase.createProductFavorite(memberId, productId);
  }

  @Transactional
  public void deleteProductFavorite(Long memberId, Long productId) {
    productDeleteFavoriteUseCase.deleteProductFavorite(memberId, productId);
  }

  @Transactional
  public List<ProductStockResponse> deductStock(
      ProductStockUpdateRequest productStockUpdateRequest) {
    return productDeductStockUseCase.deductStock(productStockUpdateRequest).stream()
        .map(productMapper::toProductStockResponse)
        .toList();
  }

  public void restoreStock(OrderDto orderDto) {
    productRestoreStockUseCase.restoreStock(orderDto);
  }

  public void createProductMember(Long memberId, String email, String name, String phoneNumber) {
    productCreateMemberUseCase.createMember(memberId, email, name, phoneNumber);
  }

  public void createProductMemberSeller(
      Long sellerId, String businessName, String representativeName) {
    productCreateSellerUseCase.createMemberSeller(sellerId, businessName, representativeName);
  }
}
