package com.modeunsa.boundedcontext.product.out.elasticsearch;

import com.modeunsa.boundedcontext.product.domain.search.document.ProductSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;

@Configuration
@RequiredArgsConstructor
public class ProductSearchIndexInitializer {

  private final ElasticsearchOperations operations;

  /* 상품 Application 시작 직후 바로 실행. ProductSearch document 기반으로 인덱싱 */
  @Bean
  @Order(0)
  public ApplicationRunner initProductSearchIndex() {
    return args -> {
      IndexOperations indexOps = operations.indexOps(ProductSearch.class);

      if (!indexOps.exists()) {
        indexOps.create();
        indexOps.putMapping(indexOps.createMapping());
      }
    };
  }
}
