package com.modeunsa.boundedcontext.product.app.search;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import com.modeunsa.boundedcontext.product.domain.search.document.ProductSearch;
import com.modeunsa.boundedcontext.product.out.search.ProductSearchRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProductSearchSupport {

  private final ProductSearchRepository productSearchRepository;
  private final ElasticsearchOperations elasticsearchOperations;

  public List<ProductSearch> search(String keyword) {
    return productSearchRepository.findByNameContainingOrDescriptionContaining(keyword, keyword);
  }

  public Page<ProductSearch> searchByKeyword(String keyword, int page, int size) {

    BoolQuery.Builder bool = QueryBuilders.bool();

    // keyword 조건
    if (StringUtils.hasText(keyword)) {
      bool.should(s -> s.match(m -> m.field("name").query(keyword)));
      bool.should(s -> s.match(m -> m.field("description").query(keyword)));
      bool.should(s -> s.match(m -> m.field("sellerBusinessName").query(keyword)));
      bool.minimumShouldMatch("1");
    }

    // saleStatus 필터
    bool.filter(f -> f.term(t -> t.field("saleStatus").value("SALE")));
    // productStatus 필터
    bool.filter(f -> f.term(t -> t.field("productStatus").value("COMPLETED")));

    NativeQuery query =
        NativeQuery.builder()
            .withQuery(bool.build()._toQuery())
            .withSort(Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")))
            .withPageable(PageRequest.of(page, size))
            .withTrackTotalHits(true)
            .build();

    SearchHits<ProductSearch> hits = elasticsearchOperations.search(query, ProductSearch.class);

    List<ProductSearch> content = hits.stream().map(SearchHit::getContent).toList();

    return new PageImpl<>(content, PageRequest.of(page, size), hits.getTotalHits());
  }

  public Page<String> autoComplete(String keyword) {
    NativeQuery query =
        NativeQuery.builder()
            .withQuery(q -> q.match(m -> m.field("nameAutoComplete").query(keyword)))
            .withPageable(PageRequest.of(0, 10))
            .build();

    SearchHits<ProductSearch> hits = elasticsearchOperations.search(query, ProductSearch.class);
    List<String> content = hits.stream().map(hit -> hit.getContent().getName()).toList();

    return new PageImpl<>(content, PageRequest.of(0, 10), hits.getTotalHits());
  }
}
