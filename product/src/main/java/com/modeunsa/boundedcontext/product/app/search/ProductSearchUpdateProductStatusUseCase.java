package com.modeunsa.boundedcontext.product.app.search;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductSearchUpdateProductStatusUseCase {

  private static final String INDEX_NAME = "product_search";
  private final ElasticsearchOperations elasticsearchOperations;

  public void updateProductStatus(String productId, String productStatus) {
    UpdateQuery updateQuery =
        UpdateQuery.builder(productId)
            .withDocument(Document.from(Map.of("productStatus", productStatus)))
            .build();

    elasticsearchOperations.update(updateQuery, IndexCoordinates.of(INDEX_NAME));
  }
}
