package com.modeunsa.boundedcontext.product.app.search;

import com.modeunsa.boundedcontext.product.domain.ProductUpdatableField;
import com.modeunsa.boundedcontext.product.in.dto.ProductUpdateRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductSearchUpdateUseCase {

  private static final String INDEX_NAME = "product_search";
  private final ElasticsearchOperations elasticsearchOperations;

  public void updateProductSearch(
      String productId, ProductUpdateRequest request, Set<String> changedFields) {
    UpdateQuery updateQuery =
        UpdateQuery.builder(productId)
            .withDocument(Document.from(this.getUpdatableMap(request, changedFields)))
            .build();

    elasticsearchOperations.update(updateQuery, IndexCoordinates.of(INDEX_NAME));
  }

  private Map<String, Object> getUpdatableMap(
      ProductUpdateRequest request, Set<String> changedFields) {
    Map<String, Object> map = new HashMap<>();
    if (changedFields.contains(ProductUpdatableField.CATEGORY.name())) {
      map.put("category", request.getCategory());
    }
    if (changedFields.contains(ProductUpdatableField.PRICE.name())) {
      map.put("price", request.getPrice());
    }
    if (changedFields.contains(ProductUpdatableField.SALE_PRICE.name())) {
      map.put("salePrice", request.getSalePrice());
    }
    if (changedFields.contains(ProductUpdatableField.SALE_STATUS.name())) {
      map.put("saleStatus", request.getSaleStatus());
    }
    if (changedFields.contains(ProductUpdatableField.IMAGES.name())) {
      map.put("primaryImageUrl", request.getImages().getFirst());
    }
    return map;
  }
}
