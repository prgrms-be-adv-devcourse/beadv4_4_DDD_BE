package com.modeunsa.boundedcontext.product.out;

import com.modeunsa.api.pagination.CursorDto;
import com.modeunsa.boundedcontext.product.domain.Product;
import org.springframework.data.domain.Slice;

public interface ProductRepositoryCustom {

  Slice<Product> searchByKeyword(String keyword, CursorDto cursor, int size);
}
