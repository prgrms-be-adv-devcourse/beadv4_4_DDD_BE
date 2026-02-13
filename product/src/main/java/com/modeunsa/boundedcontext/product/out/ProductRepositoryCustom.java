package com.modeunsa.boundedcontext.product.out;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.in.dto.ProductCursorDto;
import org.springframework.data.domain.Slice;

public interface ProductRepositoryCustom {

  Slice<Product> searchByKeyword(String keyword, ProductCursorDto cursor, int size);
}
