package com.modeunsa.boundedcontext.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InventoryTest {

  @Test
  @DisplayName("예약된 수량보다 적게 재고를 수정하면 실패해야 한다")
  void update_fail_if_less_than_reserved() {
    // given
    // 실재고 10, 예약 3인 상태 생성
    Inventory inventory = Inventory.builder().quantity(10).reservedQuantity(3).build();

    // when & then
    // 2개로 줄이려고 시도
    boolean result = inventory.updateInventory(2);

    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("정상적인 수량 변경은 성공해야 한다")
  void update_success() {
    // given
    Inventory inventory = Inventory.builder().quantity(10).reservedQuantity(3).build();

    // when & then
    // 5개로 변경
    boolean result = inventory.updateInventory(5);

    assertThat(result).isTrue();
    assertThat(inventory.getQuantity()).isEqualTo(5);
  }
}
