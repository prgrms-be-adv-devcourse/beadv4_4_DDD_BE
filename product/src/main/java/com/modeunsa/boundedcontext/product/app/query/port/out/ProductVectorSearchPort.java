package com.modeunsa.boundedcontext.product.app.query.port.out;

import com.modeunsa.boundedcontext.product.domain.search.document.ProductSearch;
import java.util.List;

public interface ProductVectorSearchPort {
  List<ProductSearch> knnSearch(String keyword, int k);
}
