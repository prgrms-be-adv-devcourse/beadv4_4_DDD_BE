package com.modeunsa.shared.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record InventoryReserveRequest(
    @NotEmpty(message = "예약할 상품 목록은 비어있을 수 없습니다.") @Valid List<Item> items) {
  public record Item(
      @NotNull(message = "상품 ID는 필수입니다.") Long productId,
      @NotNull(message = "수량은 필수입니다.") @Positive(message = "수량은 1개 이상이어야 합니다.") Integer quantity) {}
}
