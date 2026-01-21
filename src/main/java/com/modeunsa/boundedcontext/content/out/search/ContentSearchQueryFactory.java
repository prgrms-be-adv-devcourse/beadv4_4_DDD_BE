package com.modeunsa.boundedcontext.content.out.search;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

public class ContentSearchQueryFactory {

  public Query create(ContentSearchCondition condition) {
    String keyword = condition.getKeyword();

    BoolQuery boolQuery =
        BoolQuery.of(
            b ->
                b.should(MatchQuery.of(m -> m.field("text").query(keyword))._toQuery())
                    .should(MatchQuery.of(m -> m.field("tags").query(keyword))._toQuery()));

    return Query.of(q -> q.bool(boolQuery));
  }
}
