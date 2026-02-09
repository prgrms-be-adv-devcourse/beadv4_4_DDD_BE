package com.modeunsa.boundedcontext.payment.app.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface PageableRequest {
  int page();

  int size();

  default Pageable pageable() {
    int p = Math.max(page(), 0);
    int s = size() <= 0 ? 10 : size();

    return PageRequest.of(p, s, Sort.by(Sort.Order.desc(defaultSort())));
  }

  default String defaultSort() {
    return "createdAt";
  }
}
