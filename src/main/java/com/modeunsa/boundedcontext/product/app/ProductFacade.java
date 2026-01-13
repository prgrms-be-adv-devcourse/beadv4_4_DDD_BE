package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.shared.product.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductFacade {

  private final ProductCreateProductUseCase productCreateProductUseCase;

  public Product createProduct(ProductDto productDto) {
    return productCreateProductUseCase.createProduct(productDto);
  }
}
