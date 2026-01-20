package com.modeunsa.global.elasticsearch.app;

import static com.modeunsa.global.status.ErrorStatus.ELASTICSEARCH_BULKINDEX_FAILED;
import static com.modeunsa.global.status.ErrorStatus.ELASTICSEARCH_DELETE_FAILED;
import static com.modeunsa.global.status.ErrorStatus.ELASTICSEARCH_INDEX_FAILED;
import static com.modeunsa.global.status.ErrorStatus.ELASTICSEARCH_SEARCH_FAILED;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.modeunsa.global.elasticsearch.ElasticSearchPageRequest;
import com.modeunsa.global.elasticsearch.domain.ElasticSearchHit;
import com.modeunsa.global.elasticsearch.domain.ElasticSearchPage;
import com.modeunsa.global.elasticsearch.sort.ElasticSearchSort;
import com.modeunsa.global.elasticsearch.sort.ElasticSearchSortOrder;
import com.modeunsa.global.exception.GeneralException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ElasticSearchExecutorImpl implements ElasticSearchExecutor {

  private final ElasticsearchClient client;

  public ElasticSearchExecutorImpl(ElasticsearchClient client) {
    this.client = client;
  }

  @Override
  public <T> ElasticSearchPage<T> search(
      String index, Query query, ElasticSearchPageRequest pageRequest, Class<T> clazz) {
    try {
      SearchRequest request =
          SearchRequest.of(
              builder ->
                  builder
                      .index(index)
                      .query(query)
                      .from(pageRequest.getFrom())
                      .size(pageRequest.getSize())
                      .sort(toSortOptions(pageRequest.getSorts())));

      var response = client.search(request, clazz);

      long total = response.hits().total() == null ? 0L : response.hits().total().value();
      List<ElasticSearchHit<T>> hits =
          response.hits().hits().stream()
              .map(
                  hit ->
                      new ElasticSearchHit<>(
                          hit.id(), hit.score() == null ? 0.0 : hit.score(), hit.source()))
              .collect(Collectors.toList());

      return new ElasticSearchPage<>(total, hits, pageRequest.getPage(), pageRequest.getSize());
    } catch (IOException e) {
      throw new GeneralException(ELASTICSEARCH_SEARCH_FAILED.getMessage(), e); // 메세지만 출력
    }
  }

  @Override
  public <T> void index(String index, String id, T document) {
    try {
      IndexRequest<T> request =
          IndexRequest.of(builder -> builder.index(index).id(id).document(document));
      client.index(request);
    } catch (IOException e) {
      throw new GeneralException(ELASTICSEARCH_INDEX_FAILED.getMessage(), e);
    }
  }

  @Override
  public void delete(String index, String id) {
    try {
      DeleteRequest request = DeleteRequest.of(builder -> builder.index(index).id(id));
      client.delete(request);
    } catch (IOException e) {
      throw new GeneralException(ELASTICSEARCH_DELETE_FAILED.getMessage(), e);
    }
  }

  @Override
  public <T> void bulkIndex(String index, Map<String, T> idToDocument) {
    if (idToDocument == null || idToDocument.isEmpty()) {
      return;
    }

    try {
      BulkRequest.Builder builder = new BulkRequest.Builder();

      idToDocument.forEach(
          (id, document) ->
              builder.operations(
                  op -> op.index(idx -> idx.index(index).id(id).document(document))));

      client.bulk(builder.build());
    } catch (IOException e) {
      throw new GeneralException(ELASTICSEARCH_BULKINDEX_FAILED.getMessage(), e);
    }
  }

  private List<SortOptions> toSortOptions(List<ElasticSearchSort> sorts) {
    if (sorts == null || sorts.isEmpty()) {
      return List.of();
    }

    return sorts.stream()
        .map(
            sort ->
                SortOptions.of(
                    s ->
                        s.field(f -> f.field(sort.getField()).order(toSortOrder(sort.getOrder())))))
        .collect(Collectors.toList());
  }

  private SortOrder toSortOrder(ElasticSearchSortOrder order) {
    return order == ElasticSearchSortOrder.ASC ? SortOrder.Asc : SortOrder.Desc;
  }
}
