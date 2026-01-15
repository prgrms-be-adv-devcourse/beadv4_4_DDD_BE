package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.shared.product.dto.ProductRequest;
import com.modeunsa.shared.product.dto.ProductResponse;
import com.modeunsa.shared.product.dto.ProductUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductFacade {

  private final ProductCreateProductUseCase productCreateProductUseCase;
  private final ProductUpdateProductUseCase productUpdateProductUseCase;
  private final ProductSupport productSupport;
  private final ProductMapper productMapper;

  @Transactional
  public ProductResponse createProduct(Long sellerId, ProductRequest productRequest) {
    return productCreateProductUseCase.createProduct(sellerId, productRequest);
  }

  public ProductResponse getProduct(Long productId) {
    Product product = productSupport.getProduct(productId);
    return productMapper.toResponse(product);
  }

  public Page<ProductResponse> getProducts(
      Long memberId, ProductCategory category, Pageable pageable) {
    Page<Product> products = productSupport.getProducts(memberId, category, pageable);
    return products.map(productMapper::toResponse);
  }

  @Transactional
  public ProductResponse updateProduct(
      Long sellerId, Long productId, ProductUpdateRequest productRequest) {
    ProductResponse productResponse =
        productUpdateProductUseCase.updateProduct(sellerId, productId, productRequest);
    return productResponse;
  }
}
