package com.modeunsa.boundedcontext.product.app.search;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.search.document.ProductSearch;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductSearchReindexUseCase {

  private final ProductRepository productRepository;
  private final ElasticsearchOperations elasticsearchOperations;
  private final EmbeddingModel embeddingModel;

  public void reindexAll() {

    List<Product> products = productRepository.findById(1009L).stream().toList();

    List<IndexQuery> queries =
        products.stream()
            .map(
                product -> {
                  String text =
                      "%s %s %s"
                          .formatted(
                              product.getName(),
                              product.getSeller().getBusinessName(),
                              product.getDescription());
                  float[] vector = embeddingModel.embed(text);
                  ProductSearch doc = ProductSearch.from(product, vector);
                  IndexQuery query = new IndexQuery();
                  query.setId(doc.getId());
                  query.setObject(doc);
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

    String text =
        "%s %s %s"
            .formatted(
                product.getName(), product.getSeller().getBusinessName(), product.getDescription());
    float[] vector = embeddingModel.embed(text);
    ProductSearch doc = ProductSearch.from(product, vector);
    IndexQuery query = new IndexQuery();
    query.setId(doc.getId());
    query.setObject(doc);

    elasticsearchOperations.index(query, IndexCoordinates.of("product_search"));
    elasticsearchOperations.indexOps(IndexCoordinates.of("product_search")).refresh();
  }
}
