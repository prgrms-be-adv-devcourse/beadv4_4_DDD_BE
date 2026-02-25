package com.modeunsa.boundedcontext.product.app.query.port.out;

import org.springframework.data.domain.Page;

public interface ProductAutoCompletePort {
  Page<String> autoComplete(String keyword);
}
