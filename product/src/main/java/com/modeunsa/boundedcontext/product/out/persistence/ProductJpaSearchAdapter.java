package com.modeunsa.boundedcontext.product.out.persistence;

import com.modeunsa.api.pagination.CursorDto;
import com.modeunsa.boundedcontext.product.app.query.port.out.ProductKeywordSearchPort;
import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component("jpaSearchAdapter")
@RequiredArgsConstructor
public class ProductJpaSearchAdapter implements ProductKeywordSearchPort {

  private final ProductRepository productRepository;

  @Override
  public Slice<Product> search(String keyword, CursorDto cursor, int size) {
    return productRepository.searchByKeyword(keyword, cursor, size);
  }
}
