package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.shared.product.dto.ProductRequest;
import com.modeunsa.shared.product.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductFacade {

  private final ProductCreateProductUseCase productCreateProductUseCase;
  private final ProductSupport productSupport;
  private final ProductMapper productMapper;

  @Transactional
  public ProductResponse createProduct(ProductRequest productRequest) {
    return productCreateProductUseCase.createProduct(productRequest);
  }

  public ProductResponse getProduct(Long productId) {
    Product product = productSupport.getProduct(productId);
    return productMapper.toResponse(product);
  }

  public Page<ProductResponse> getProducts(ProductCategory category, Pageable pageable) {
    Page<Product> products = productSupport.getProducts(category, pageable);
    return products.map(productMapper::toResponse);
  }
}
