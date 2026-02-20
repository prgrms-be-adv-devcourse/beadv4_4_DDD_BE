package com.modeunsa.boundedcontext.product.app.query.port.out;

import com.modeunsa.api.pagination.CursorDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

public interface ProductSearchPort<T> {

  Slice<T> search(String keyword, CursorDto cursor, int size);

  Page<String> autoComplete(String keyword);
}
