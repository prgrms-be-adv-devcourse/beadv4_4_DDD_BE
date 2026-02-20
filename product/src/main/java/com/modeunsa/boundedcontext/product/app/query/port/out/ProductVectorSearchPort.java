package com.modeunsa.boundedcontext.product.app.query.port.out;

import com.modeunsa.api.pagination.VectorCursorDto;
import com.modeunsa.boundedcontext.product.domain.search.document.ProductSearch;
import java.util.List;
import org.springframework.data.domain.Slice;
import org.springframework.data.elasticsearch.core.SearchHit;

public interface ProductVectorSearchPort {
  List<ProductSearch> knnSearch(String keyword, int k);

  Slice<SearchHit<ProductSearch>> hybridSearch(String keyword, VectorCursorDto dto, int size);
}
