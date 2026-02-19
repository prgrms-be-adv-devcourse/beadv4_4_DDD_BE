package com.modeunsa.boundedcontext.product.app.search;

import com.modeunsa.api.pagination.CursorDto;
import com.modeunsa.boundedcontext.product.app.query.port.out.ProductSearchPort;
import com.modeunsa.boundedcontext.product.domain.search.document.ProductSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

  private final ProductSearchPort productSearchPort;

  public Slice<ProductSearch> searchByKeyword(String keyword, CursorDto cursor, int size) {
    return productSearchPort.search(keyword, cursor, size);
  }

  public Page<String> autoComplete(String keyword) {
    return productSearchPort.autoComplete(keyword);
  }
}
