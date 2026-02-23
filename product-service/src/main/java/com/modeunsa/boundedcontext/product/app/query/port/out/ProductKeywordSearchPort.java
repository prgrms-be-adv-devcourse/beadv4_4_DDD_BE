package com.modeunsa.boundedcontext.product.app.query.port.out;

import com.modeunsa.api.pagination.KeywordCursorDto;
import org.springframework.data.domain.Slice;

public interface ProductKeywordSearchPort<T> {

  Slice<T> search(String keyword, KeywordCursorDto cursor, int size);
}
