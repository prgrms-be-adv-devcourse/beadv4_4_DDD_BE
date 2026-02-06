package com.modeunsa.boundedcontext.product.elasticsearch.app;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.modeunsa.boundedcontext.product.elasticsearch.ElasticSearchPageRequest;
import com.modeunsa.boundedcontext.product.elasticsearch.model.ElasticSearchPage;
import java.util.Map;

public interface ElasticSearchExecutor {

  <T> ElasticSearchPage<T> search(
      Query query,
      // page, size, sort
      ElasticSearchPageRequest pageRequest,
      // _source 어떤 타입으로 매핑할 지
      Class<T> clazz);

  // 단 건
  <T> void index(String index, String id, T document);

  void delete(String index, String id);

  // 대량 인덱싱
  <T> void bulkIndex(String index, Map<String, T> idToDocument);
}
