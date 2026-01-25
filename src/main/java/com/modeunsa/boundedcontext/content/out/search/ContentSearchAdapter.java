package com.modeunsa.boundedcontext.content.out.search;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.modeunsa.global.elasticsearch.ElasticSearchPageRequest;
import com.modeunsa.global.elasticsearch.IndexName;
import com.modeunsa.global.elasticsearch.app.ElasticSearchExecutor;
import com.modeunsa.global.elasticsearch.model.ElasticSearchPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContentSearchAdapter implements ContentSearchUseCase {

  private final ElasticSearchExecutor elasticSearchExecutor;
  private final ContentSearchQueryFactory queryFactory;

  @Override
  public ElasticSearchPage<ContentSearchDocument> search(ContentSearchCondition condition) {
    Query query = queryFactory.create(condition);

    ElasticSearchPageRequest pageRequest =
        ElasticSearchPageRequest.of(condition.getPage(), condition.getSize());

    return elasticSearchExecutor.search(
        IndexName.CONTENT.description(), query, pageRequest, ContentSearchDocument.class);
  }
}
