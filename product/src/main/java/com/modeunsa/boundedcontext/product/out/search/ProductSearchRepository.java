package com.modeunsa.boundedcontext.product.out.search;

import com.modeunsa.boundedcontext.product.domain.search.document.ProductSearch;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

@ConditionalOnProperty(name = "app.elasticsearch.enabled", havingValue = "true")
public interface ProductSearchRepository extends ElasticsearchRepository<ProductSearch, String> {
  List<ProductSearch> findAll();

  List<ProductSearch> findByNameContainingOrDescriptionContaining(String name, String description);
}
