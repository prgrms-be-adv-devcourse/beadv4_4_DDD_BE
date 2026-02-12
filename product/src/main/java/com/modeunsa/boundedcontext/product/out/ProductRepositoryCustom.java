package com.modeunsa.boundedcontext.product.out;

import com.modeunsa.boundedcontext.product.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {

  Page<Product> searchByKeyword(String keyword, Pageable pageable);
}
