package com.modeunsa.boundedcontext.inventory.app;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.modeunsa.shared.inventory.dto.InventoryReserveRequest;
import com.modeunsa.shared.order.dto.OrderItemDto;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
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
                "상품이름",
                3, // quantity
                BigDecimal.valueOf(10000))));

    // then
    String remaining = redisTemplate.opsForValue().get("inventory:available:" + productId);

    assertThat(Integer.parseInt(remaining)).isEqualTo(10);

    System.out.println("===== 주문 취소 확정 후 =====");
    printRedisStock(productId);
  }

  @Test
  @DisplayName("멀티 유저 동시 주문 + 일부 취소")
  void concurrentReserveAndReleaseWithAccurateSuccessTest() throws InterruptedException {
    // 초기 재고 세팅
    Map<Long, Integer> initialStock =
        Map.of(
            1L, 50,
            2L, 30,
            3L, 40);

    initialStock.forEach(
        (pid, qty) ->
            redisTemplate.opsForValue().set("inventory:available:" + pid, String.valueOf(qty)));

    System.out.println("===== 초기 재고 =====");
    initialStock
        .keySet()
        .forEach(
            pid -> {
              System.out.printf(
                  "Redis 재고 [inventory:available:%d] = %s%n",
                  pid, redisTemplate.opsForValue().get("inventory:available:" + pid));
            });

    int threadCount = 100;
    ExecutorService executor = Executors.newFixedThreadPool(32);
    CountDownLatch latch = new CountDownLatch(threadCount);

    Random random = new Random();
    List<Event> timeline = Collections.synchronizedList(new ArrayList<>());

    // 상품별 성공/취소 집계용
    Map<Long, AtomicInteger> successCount = new ConcurrentHashMap<>();
    Map<Long, AtomicInteger> cancelCount = new ConcurrentHashMap<>();
    initialStock
        .keySet()
        .forEach(
            pid -> {
              successCount.put(pid, new AtomicInteger(0));
              cancelCount.put(pid, new AtomicInteger(0));
            });

    for (int i = 0; i < threadCount; i++) {
      executor.submit(
          () -> {
            long threadId = Thread.currentThread().getId();
            try {
              // 랜덤으로 1~3개 상품 선택, qty=1
              List<InventoryReserveRequest.Item> items =
                  initialStock.keySet().stream()
                      .filter(pid -> random.nextBoolean())
                      .map(pid -> new InventoryReserveRequest.Item(pid, 1))
                      .toList();

              if (!items.isEmpty()) {
                try {
                  inventoryReserveInventoryUseCase.reserveInventory(
                      new InventoryReserveRequest(items));

                  long ts = System.currentTimeMillis();
                  for (InventoryReserveRequest.Item item : items) {
                    int remaining =
                        Integer.parseInt(
                            redisTemplate
                                .opsForValue()
                                .get("inventory:available:" + item.productId()));
                    timeline.add(
                        new Event(
                            "RESERVE", threadId, ts, item.productId(), item.quantity(), remaining));
                    successCount.get(item.productId()).incrementAndGet();
                  }

                  // 10% 확률로 취소
                  if (random.nextInt(10) == 0) {
                    List<OrderItemDto> cancelItems =
                        items.stream()
                            .map(
                                item ->
                                    new OrderItemDto(
                                        0L,
                                        item.productId(),
                                        1L,
                                        "상품이름",
                                        item.quantity(),
                                        BigDecimal.valueOf(10000)))
                            .toList();

                    inventoryReleaseInventoryUseCase.releaseInventory(cancelItems);
                    long tsCancel = System.currentTimeMillis();

                    for (OrderItemDto dto : cancelItems) {
                      int remaining =
                          Integer.parseInt(
                              redisTemplate
                                  .opsForValue()
                                  .get("inventory:available:" + dto.getProductId()));
                      timeline.add(
                          new Event(
                              "RELEASE",
                              threadId,
                              tsCancel,
                              dto.getProductId(),
                              dto.getQuantity(),
                              remaining));
                      cancelCount.get(dto.getProductId()).incrementAndGet();
                    }
                  }

                } catch (Exception ignored) {
                  // 예약 실패 시 이벤트 기록만
                  long tsFail = System.currentTimeMillis();
                  for (InventoryReserveRequest.Item item : items) {
                    timeline.add(
                        new Event(
                            "FAIL",
                            threadId,
                            tsFail,
                            item.productId(),
                            item.quantity(),
                            Integer.parseInt(
                                redisTemplate
                                    .opsForValue()
                                    .get("inventory:available:" + item.productId()))));
                  }
                }
              }

            } finally {
              latch.countDown();
            }
          });
    }

    latch.await();
    executor.shutdown();

    // 타임라인 정렬 (timestamp 기준)
    timeline.sort(Comparator.comparingLong(e -> e.timestamp));

    System.out.println("===== 재고 변화 타임라인 =====");
    for (Event e : timeline) {
      System.out.printf(
          "[%d] %s productId=%d, qty=%d, remaining=%d%n",
          e.threadId, e.type, e.productId, e.qty, e.remaining);
    }

    System.out.println("===== 최종 재고 =====");
    initialStock.keySet().stream()
        .sorted() // productId 기준 오름차순
        .forEach(
            pid -> {
              int remaining =
                  Integer.parseInt(redisTemplate.opsForValue().get("inventory:available:" + pid));
              System.out.printf(
                  "상품 %d 최종 재고=%d, 총 예약 성공=%d, 총 취소 복구=%d%n",
                  pid, remaining, successCount.get(pid).get(), cancelCount.get(pid).get());
            });
  }

  private void printRedisStock(Long productId) {
    String key = "inventory:available:" + productId;
    String value = redisTemplate.opsForValue().get(key);
    System.out.println("Redis 재고 [" + key + "] = " + value);
  }
}
