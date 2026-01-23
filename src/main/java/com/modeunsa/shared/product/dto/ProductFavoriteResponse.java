package com.modeunsa.shared.product.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ProductFavoriteResponse {
  private final Long memberId;
  private final Long productId;
  private final Long productName;
  private final boolean isFavorite;
}
