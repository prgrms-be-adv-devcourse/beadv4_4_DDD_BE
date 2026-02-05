package com.modeunsa.boundedcontext.product.elasticsearch;

import com.modeunsa.boundedcontext.product.elasticsearch.sort.ElasticSearchSort;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ElasticSearchPageRequest {
  private final int page;
  private final int size;
  private final List<ElasticSearchSort> sorts;

  private ElasticSearchPageRequest(int page, int size, List<ElasticSearchSort> sorts) {

    // 음수 페이지 요청 방지
    this.page = Math.max(page, 0);

    // 한 page당 최소 1개, 최대 100개의 결과를 보여줌
    this.size = Math.min(Math.max(size, 1), 100);
    this.sorts = sorts == null ? Collections.emptyList() : new ArrayList<>(sorts);
  }

  // 기본 정렬(관련도 _score순)
  public static ElasticSearchPageRequest of(int page, int size) {
    return new ElasticSearchPageRequest(page, size, Collections.emptyList());
  }

  // 정렬 지정
  public static ElasticSearchPageRequest of(int page, int size, List<ElasticSearchSort> sorts) {
    return new ElasticSearchPageRequest(page, size, sorts);
  }

  public int getFrom() {
    return page * size;
  }

  public int getSize() {
    return size;
  }

  public List<ElasticSearchSort> getSorts() {
    return Collections.unmodifiableList(sorts);
  }

  public int getPage() {
    return page;
  }
}
