package com.modeunsa.boundedcontext.product.out;

import com.modeunsa.api.pagination.KeywordCursorDto;
import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ProductRepositoryCustom {

  Slice<Product> searchByKeyword(String keyword, KeywordCursorDto cursor, int size);

  Page<Product> searchByConditions(ProductCategory category, Pageable pageable);
}
