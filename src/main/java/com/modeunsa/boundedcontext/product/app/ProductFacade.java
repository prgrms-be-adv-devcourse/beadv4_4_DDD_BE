package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.shared.product.dto.ProductRequest;
import com.modeunsa.shared.product.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductFacade {

  private final ProductCreateProductUseCase productCreateProductUseCase;

  @Transactional
  public ProductResponse createProduct(ProductRequest productRequest) {
    return productCreateProductUseCase.createProduct(productRequest);
  }
}
