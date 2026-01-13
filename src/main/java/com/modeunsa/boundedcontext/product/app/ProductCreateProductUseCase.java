package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import com.modeunsa.shared.product.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductCreateProductUseCase {

  private final ProductRepository productRepository;
  private final ProductMapper productMapper;

  @Transactional
  public Product createProduct(ProductDto productDto) {

    Product product = productMapper.toEntity(productDto);
    // TODO: 파일 업로드 작업 이후에 이미지 추가 예정
    return productRepository.save(product);
  }
}
