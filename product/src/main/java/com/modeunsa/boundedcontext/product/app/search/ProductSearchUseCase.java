package com.modeunsa.boundedcontext.product.app.search;

import com.modeunsa.boundedcontext.product.domain.search.document.ProductSearch;
import com.modeunsa.boundedcontext.product.out.search.ProductSearchRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@ConditionalOnProperty(name = "app.elasticsearch.enabled", havingValue = "true")
@Service
@RequiredArgsConstructor
public class ProductSearchUseCase {

  private final ProductSearchRepository productSearchRepository;

  public ProductSearch createProductSearch(
      String name, String description, String category, String saleStatus, BigDecimal price) {

    ProductSearch productSearch = new ProductSearch(name, description, category, saleStatus, price);
    return productSearchRepository.save(productSearch);
  }

  public List<ProductSearch> search(String keyword) {
    return productSearchRepository.findByNameContainingOrDescriptionContaining(keyword, keyword);
  }
}
