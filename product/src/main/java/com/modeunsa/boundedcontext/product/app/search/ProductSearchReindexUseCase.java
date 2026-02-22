package com.modeunsa.boundedcontext.product.app.search;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.search.document.ProductSearch;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchReindexUseCase {

  private final ProductRepository productRepository;
  private final ElasticsearchOperations elasticsearchOperations;
  private final EmbeddingModel embeddingModel;

  public void reindexAll() {

    List<Product> products = productRepository.findAll();

    List<IndexQuery> queries =
        products.stream()
            .map(
                product -> {
                  float[] vector = this.getVector(product);
                  ProductSearch doc = ProductSearch.from(product, vector);
                  IndexQuery query = new IndexQuery();
                  query.setId(doc.getId());
                  query.setObject(doc);
                  log.debug("doc.getId() : {}", doc.getId());
                  return query;
                })
            .toList();

    elasticsearchOperations.bulkIndex(queries, IndexCoordinates.of("product_search"));

    elasticsearchOperations.indexOps(IndexCoordinates.of("product_search")).refresh();
  }

  public void reindexById(Long id) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new GeneralException(ErrorStatus.PRODUCT_NOT_FOUND));
    float[] vector = this.getVector(product);
    ProductSearch doc = ProductSearch.from(product, vector);
    IndexQuery query = new IndexQuery();
    query.setId(doc.getId());
    query.setObject(doc);

    elasticsearchOperations.index(query, IndexCoordinates.of("product_search"));
    elasticsearchOperations.indexOps(IndexCoordinates.of("product_search")).refresh();
  }

  private float[] getVector(Product product) {
    float[] vector = null;
    // 주문 가능한 (=검색 가능한) 상품이 아닌 경우 embedding 생성 X
    if (product.isOrderAvailable()) {
      String text =
          "%s %s %s"
              .formatted(
                  product.getName(),
                  product.getSeller().getBusinessName(),
                  product.getDescription());
      vector = embeddingModel.embed(text);
      log.debug("embed succeeded: {}", product.getId());
    }
    return vector;
  }
}
