package com.modeunsa.boundedcontext.content.out.search;

import com.modeunsa.global.elasticsearch.model.ElasticSearchPage;

// 어떻게 검색할지 규칙 및 실행 주체
public interface ContentSearchUseCase {

  ElasticSearchPage<ContentSearchDocument> search(ContentSearchCondition condition);
}
