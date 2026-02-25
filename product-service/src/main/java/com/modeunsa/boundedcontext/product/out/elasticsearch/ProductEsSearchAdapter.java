package com.modeunsa.boundedcontext.product.out.elasticsearch;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import com.modeunsa.api.pagination.KeywordCursorDto;
import com.modeunsa.api.pagination.VectorCursorDto;
import com.modeunsa.boundedcontext.product.app.query.port.out.ProductAutoCompletePort;
import com.modeunsa.boundedcontext.product.app.query.port.out.ProductKeywordSearchPort;
import com.modeunsa.boundedcontext.product.app.query.port.out.ProductVectorSearchPort;
import com.modeunsa.boundedcontext.product.domain.search.document.ProductSearch;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Primary;
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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component("esSearchAdapter")
@RequiredArgsConstructor
@Primary
public class ProductEsSearchAdapter
    implements ProductKeywordSearchPort, ProductAutoCompletePort, ProductVectorSearchPort {
  private final ElasticsearchOperations elasticsearchOperations;
  private final EmbeddingModel embeddingModel;

  @Override
  public Slice<ProductSearch> search(String keyword, KeywordCursorDto cursor, int size) {

    BoolQuery.Builder bool = QueryBuilders.bool();
    bool = this.buildKeywordQuery(bool, keyword);

    NativeQueryBuilder queryBuilder =
        NativeQuery.builder()
            .withQuery(bool.build()._toQuery())
            .withSort(Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")))
            .withMaxResults(size);

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
    this.applyOrderableFilters(bool);

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

  @Override
  public List<ProductSearch> knnSearch(String keyword, int k) {
    if (!StringUtils.hasText(keyword)) {
      return List.of();
    }

    float[] vector = embeddingModel.embed(keyword);
    BoolQuery.Builder bool = QueryBuilders.bool();
    List<Float> list = new ArrayList<>();

    for (float v : vector) {
      list.add(v);
    }

    bool.should(s -> s.knn(m -> m.field("embedding").queryVector(list).k(k).numCandidates(100)));

    NativeQuery query =
        NativeQuery.builder()
            .withQuery(bool.build()._toQuery())
            .withSort(Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")))
            .withPageable(PageRequest.of(0, 10))
            .build();

    SearchHits<ProductSearch> hits = elasticsearchOperations.search(query, ProductSearch.class);
    return hits.getSearchHits().stream().map(SearchHit::getContent).toList();
  }

  @Override
  public Slice<SearchHit<ProductSearch>> hybridSearch(
      String keyword, VectorCursorDto cursor, int size) {
    // Query Embedding 생성
    float[] vector = embeddingModel.embed(keyword);

    BoolQuery.Builder bool = QueryBuilders.bool();
    bool = this.buildKeywordQuery(bool, keyword);

    List<Float> list = new ArrayList<>();
    for (float v : vector) {
      list.add(v);
    }

    int fetchSize = size + 1;
    NativeQueryBuilder queryBuilder =
        NativeQuery.builder()
            .withQuery(bool.build()._toQuery())
            .withKnnSearches(
                knn ->
                    knn.field("embedding")
                        .queryVector(list)
                        .k(size * 3)
                        .numCandidates(size * 10)
                        .boost(3.0f))
            .withSort(Sort.by(Sort.Order.desc("_score"), Sort.Order.desc("id")))
            .withMaxResults(fetchSize);

    if (cursor != null) {
      queryBuilder.withSearchAfter(List.of(cursor.score(), cursor.id()));
    }

    NativeQuery query = queryBuilder.build();

    List<SearchHit<ProductSearch>> searchHits =
        elasticsearchOperations.search(query, ProductSearch.class).getSearchHits();

    boolean hasNext = searchHits.size() > size;
    List<SearchHit<ProductSearch>> content = hasNext ? searchHits.subList(0, size) : searchHits;

    return new SliceImpl<>(content, Pageable.unpaged(), hasNext);
  }

  @Override
  public List<ProductSearch> searchByVector(String text, int k) {

    // embedding 생성
    float[] vector = embeddingModel.embed(text);

    List<Float> list = new ArrayList<>();
    for (float v : vector) {
      list.add(v);
    }

    NativeQuery query =
        NativeQuery.builder()
            .withKnnSearches(
                knn -> knn.field("embedding").queryVector(list).k(k * 10).numCandidates(k * 10))
            .build();

    SearchHits<ProductSearch> hits = elasticsearchOperations.search(query, ProductSearch.class);

    return hits.getSearchHits().stream().map(SearchHit::getContent).toList();
  }

  private BoolQuery.Builder buildKeywordQuery(BoolQuery.Builder bool, String keyword) {
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

      // 초성 검색
      if (isChosung(keyword)) {
        bool.should(s -> s.prefix(p -> p.field("nameChosung").value(keyword).boost(4.0f)));
      }

      bool.minimumShouldMatch("1");
    }

    this.applyOrderableFilters(bool);
    return bool;
  }

  private void applyOrderableFilters(BoolQuery.Builder bool) {
    // saleStatus 필터
    bool.filter(f -> f.term(t -> t.field("saleStatus").value("SALE")));
    // productStatus 필터
    bool.filter(f -> f.term(t -> t.field("productStatus").value("COMPLETED")));
  }

  private boolean isChosung(String keyword) {
    return keyword.matches("^[ㄱ-ㅎ]+$");
  }
}
