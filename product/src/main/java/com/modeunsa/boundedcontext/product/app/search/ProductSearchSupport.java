package com.modeunsa.boundedcontext.product.app.search;

import com.modeunsa.boundedcontext.product.domain.search.document.ProductSearch;
import com.modeunsa.boundedcontext.product.out.search.ProductSearchRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductSearchSupport {

  private final ProductSearchRepository productSearchRepository;

  public List<ProductSearch> search(String keyword) {
    return productSearchRepository.findByNameContainingOrDescriptionContaining(keyword, keyword);
  }
}
