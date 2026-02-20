package com.modeunsa.boundedcontext.product.app.search;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.search.document.ProductSearch;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
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

    List<Product> products = productRepository.findAll();

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
}
