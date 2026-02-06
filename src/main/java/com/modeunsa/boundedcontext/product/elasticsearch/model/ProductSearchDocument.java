package com.modeunsa.boundedcontext.product.elasticsearch.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductSearchDocument {

  private String id;
  private String name;
  private Long price;
}
