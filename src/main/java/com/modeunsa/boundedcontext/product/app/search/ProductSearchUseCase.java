package com.modeunsa.boundedcontext.product.app.search;

import com.modeunsa.boundedcontext.product.domain.search.document.ProductSearch;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import com.modeunsa.boundedcontext.product.out.search.ProductSearchRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductSearchUseCase {

  private final ProductRepository productRepository;
  private final ProductSearchRepository productSearchRepository;

  public ProductSearch createproductSearch(
      String name, String description, String category, String saleStatus, BigDecimal price) {

    ProductSearch productSearch = new ProductSearch(name, description, category, saleStatus, price);
    return productSearchRepository.save(productSearch);
  }

  public List<ProductSearch> findAll() {
    return productSearchRepository.findAll();
  }

  public Optional<ProductSearch> findById(String id) {
    return productSearchRepository.findById(id);
  }

  public List<ProductSearch> search(String keyword) {
    return productSearchRepository.findByNameContainingOrDescriptionContaining(keyword, keyword);
  }
}
