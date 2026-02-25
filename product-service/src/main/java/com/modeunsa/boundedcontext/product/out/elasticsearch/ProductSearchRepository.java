package com.modeunsa.boundedcontext.product.out.elasticsearch;

import com.modeunsa.boundedcontext.product.domain.search.document.ProductSearch;
import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

// @ConditionalOnProperty(name = "app.elasticsearch.enabled", havingValue = "true")
public interface ProductSearchRepository extends ElasticsearchRepository<ProductSearch, String> {
  List<ProductSearch> findAll();
}
