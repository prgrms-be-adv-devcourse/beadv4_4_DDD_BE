package com.modeunsa.boundedcontext.product.in.api.v2;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.modeunsa.boundedcontext.product.elasticsearch.ElasticSearchPageRequest;
import com.modeunsa.boundedcontext.product.elasticsearch.app.ElasticSearchExecutor;
import com.modeunsa.boundedcontext.product.elasticsearch.model.ElasticSearchPage;
import com.modeunsa.boundedcontext.product.elasticsearch.model.ProductSearchDocument;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Product Search", description = "검색 상품 도메인 API")
@RestController
@RequestMapping("/api/v1/products/searches")
@RequiredArgsConstructor
public class ProductSearchController {
  private final ElasticSearchExecutor elasticSearchExecutor;

  @GetMapping
  public ResponseEntity<ElasticSearchPage<ProductSearchDocument>> searchProducts(
      @RequestParam String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    // 1. ES Query 생성 (상품명 기준)
    Query query = Query.of(q -> q.match(m -> m.field("name").query(keyword)));

    // 2. page / size
    ElasticSearchPageRequest pageRequest = ElasticSearchPageRequest.of(page, size);

    // 3. 검색 실행
    ElasticSearchPage<ProductSearchDocument> result =
        elasticSearchExecutor.search(query, pageRequest, ProductSearchDocument.class);

    return ResponseEntity.ok(result);
  }
}
