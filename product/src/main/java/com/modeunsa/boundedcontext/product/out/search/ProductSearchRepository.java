package com.modeunsa.boundedcontext.product.out.search;

import com.modeunsa.boundedcontext.product.domain.search.document.ProductSearch;
import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductSearch, String> {
  List<ProductSearch> findAll();

  List<ProductSearch> findByNameContainingOrDescriptionContaining(String name, String description);
}
