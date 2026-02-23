package com.modeunsa.boundedcontext.product.app.search;

import com.modeunsa.api.pagination.KeywordCursorDto;
import com.modeunsa.api.pagination.VectorCursorDto;
import com.modeunsa.boundedcontext.product.app.query.port.out.ProductAutoCompletePort;
import com.modeunsa.boundedcontext.product.app.query.port.out.ProductKeywordSearchPort;
import com.modeunsa.boundedcontext.product.app.query.port.out.ProductVectorSearchPort;
import com.modeunsa.boundedcontext.product.domain.search.document.ProductSearch;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

  private final @Qualifier("esSearchAdapter") ProductKeywordSearchPort productKeywordSearchPort;
  private final ProductAutoCompletePort productAutoCompletePort;
  private final ProductVectorSearchPort productVectorSearchPort;

  public Slice<ProductSearch> keywordSearch(String keyword, KeywordCursorDto cursor, int size) {
    return productKeywordSearchPort.search(keyword, cursor, size);
  }

  public Page<String> autoComplete(String keyword) {
    return productAutoCompletePort.autoComplete(keyword);
  }

  public List<ProductSearch> knnSearch(String keyword, int k) {
    return productVectorSearchPort.knnSearch(keyword, k);
  }

  public Slice<SearchHit<ProductSearch>> hybridSearch(
      String keyword, VectorCursorDto dto, int size) {
    return productVectorSearchPort.hybridSearch(keyword, dto, size);
  }
}
