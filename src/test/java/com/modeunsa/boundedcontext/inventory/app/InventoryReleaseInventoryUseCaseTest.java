package com.modeunsa.boundedcontext.inventory.app;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.modeunsa.shared.inventory.dto.InventoryReserveRequest;
import com.modeunsa.shared.order.dto.OrderItemDto;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@Tag("ignore")
@SpringBootTest
@ActiveProfiles("test")
public class InventoryReleaseInventoryUseCaseTest {
  @Autowired private RedisTemplate<String, String> redisTemplate;
  @Autowired private InventoryReserveInventoryUseCase inventoryReserveInventoryUseCase;
  @Autowired private InventoryReleaseInventoryUseCase inventoryReleaseInventoryUseCase;

  private static class Event {
    String type;
    long threadId;
    long timestamp;
    long productId;
    int qty;
    int remaining;

    Event(String type, long threadId, long timestamp, long productId, int qty, int remaining) {
      this.type = type;
      this.threadId = threadId;
      this.timestamp = timestamp;
      this.productId = productId;
      this.qty = qty;
      this.remaining = remaining;
    }
  }

  @BeforeEach
  void cleanRedis() {
    // 초기화할 상품 ID
    List<Long> productIds = List.of(1L, 2L, 3L);
    productIds.forEach(pid -> redisTemplate.delete("inventory:available:" + pid));
  }

  @Test
  @DisplayName("주문 취소 확정 시 예약 재고가 정확히 복귀된다")
  void cancelReservation_restoreStock() {
    // given
    Long productId = 1L;
    redisTemplate.opsForValue().set("inventory:available:" + productId, "10");
    System.out.println("===== 초기 상태 =====");
    printRedisStock(productId);

    // 예약 3개
    inventoryReserveInventoryUseCase.reserveInventory(
        new InventoryReserveRequest(List.of(new InventoryReserveRequest.Item(productId, 3))));

    System.out.println("===== 주문 후 (예약 완료) =====");
    printRedisStock(productId);

    // when - 주문 취소 확정
    inventoryReleaseInventoryUseCase.releaseInventory(
        List.of(
            new OrderItemDto(
                1L, // orderItemId
                productId,
                1L, // sellerId
                3, // quantity
                BigDecimal.valueOf(10000))));

    // then
    String remaining = redisTemplate.opsForValue().get("inventory:available:" + productId);

    assertThat(Integer.parseInt(remaining)).isEqualTo(10);

    System.out.println("===== 주문 취소 확정 후 =====");
    printRedisStock(productId);
  }

  private void printRedisStock(Long productId) {
    String key = "inventory:available:" + productId;
    String value = redisTemplate.opsForValue().get(key);
    System.out.println("Redis 재고 [" + key + "] = " + value);
  }
}
