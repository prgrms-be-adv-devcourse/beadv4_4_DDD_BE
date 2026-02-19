package com.modeunsa.boundedcontext.product.app.search;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import com.modeunsa.api.pagination.CursorDto;
import com.modeunsa.boundedcontext.product.domain.search.document.ProductSearch;
import com.modeunsa.boundedcontext.product.out.search.ProductSearchRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
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

  public Slice<ProductSearch> searchByKeyword(String keyword, CursorDto cursor, int size) {

    BoolQuery.Builder bool = QueryBuilders.bool();

    // keyword 조건
    if (StringUtils.hasText(keyword)) {
      // 정확 검색
      bool.should(s -> s.match(m -> m.field("name").query(keyword).boost(5.0f)));
      bool.should(s -> s.match(m -> m.field("sellerBusinessName").query(keyword).boost(5.0f)));
      bool.should(s -> s.match(m -> m.field("description").query(keyword).boost(5.0f)));

      // fuzzy 보조 검색 (오탈자) -> 상품명, 브랜드명 한정
      if (keyword.length() >= 2) {
        bool.should(
            s ->
                s.match(
                    m ->
                        m.field("name")
                            .query(keyword)
                            .fuzziness("1")
                            .prefixLength(1)
                            .maxExpansions(50)
                            .boost(1.0f)));
        bool.should(
            s ->
                s.match(
                    m ->
                        m.field("sellerBusinessName")
                            .query(keyword)
                            .fuzziness("1")
                            .prefixLength(1)
                            .maxExpansions(50)
                            .boost(1.0f)));
      }

      bool.minimumShouldMatch("1");
    }

    // saleStatus 필터
    bool.filter(f -> f.term(t -> t.field("saleStatus").value("SALE")));
    // productStatus 필터
    bool.filter(f -> f.term(t -> t.field("productStatus").value("COMPLETED")));

    NativeQueryBuilder queryBuilder =
        NativeQuery.builder()
            .withQuery(bool.build()._toQuery())
            .withSort(Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")))
            .withPageable(PageRequest.of(0, size));

    if (cursor != null) {
      queryBuilder.withSearchAfter(List.of(cursor.createdAt(), cursor.id()));
    }

    NativeQuery query = queryBuilder.build();

    SearchHits<ProductSearch> hits = elasticsearchOperations.search(query, ProductSearch.class);

    List<ProductSearch> content = hits.stream().map(SearchHit::getContent).toList();

    boolean hasNext = content.size() == size;

    return new SliceImpl<>(content, Pageable.unpaged(), hasNext);
  }

  public Page<String> autoComplete(String keyword) {
    BoolQuery.Builder bool = QueryBuilders.bool();

    // keyword 조건
    if (StringUtils.hasText(keyword)) {
      bool.should(s -> s.match(m -> m.field("nameAutoComplete").query(keyword)));
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
            .withPageable(PageRequest.of(0, 10))
            .build();

    SearchHits<ProductSearch> hits = elasticsearchOperations.search(query, ProductSearch.class);
    List<String> content = hits.stream().map(hit -> hit.getContent().getName()).toList();

    return new PageImpl<>(content, PageRequest.of(0, 10), hits.getTotalHits());
  }
}
