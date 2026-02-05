package com.modeunsa.boundedcontext.inventory.app;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.modeunsa.shared.inventory.dto.InventoryReserveRequest;
import com.modeunsa.shared.inventory.dto.InventoryReserveRequest.Item;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

// @Tag("ignore")
@SpringBootTest
@ActiveProfiles("test")
public class InventoryReserveInventoryUseCaseTest {

  @Autowired private InventoryReserveInventoryUseCase reserveUseCase;

  @Autowired private RedisTemplate<String, String> redisTemplate;

  private final List<Long> productIds = List.of(1L, 2L, 3L);
  private final Map<Long, Integer> initialStocks =
      Map.of(
          1L, 50,
          2L, 80,
          3L, 100);

  @BeforeEach
  void setUp() {
    // [Given] 재고 세팅
    initialStocks.forEach(
        (productId, qty) -> {
          redisTemplate.opsForValue().set("inventory:available:" + productId, String.valueOf(qty));
        });
  }

  @AfterEach
  void tearDown() {
    // Redis 초기화
    productIds.forEach(id -> redisTemplate.delete("inventory:available:" + id));
  }

  @Test
  @DisplayName("100명이 동시에 1개씩 주문(예약)하면, redis재고가 0이 되어야 한다.")
  void concurrentReservationTest() throws InterruptedException {
    // [When] 100명(멀티 스레드)
    int threadCount = 100;
    // 스레드 풀 생성
    ExecutorService executorService = Executors.newFixedThreadPool(32);
    // 모든 스레드가 끝날 때까지 기다리는 장치
    CountDownLatch latch = new CountDownLatch(threadCount);

    AtomicInteger successCount = new AtomicInteger(); // 성공 횟수 카운터
    AtomicInteger failCount = new AtomicInteger(); // 실패 횟수 카운터

    Long productId = 1L;
    for (int i = 0; i < threadCount; i++) {
      executorService.submit(
          () -> {
            try {
              // 동시에 예약 요청
              reserveUseCase.reserveInventory(
                  new InventoryReserveRequest(Collections.singletonList(new Item(productId, 1))));
              successCount.getAndIncrement();
            } catch (Exception e) {
              System.out.println("❌ 실패 로그: " + e.getMessage());
              failCount.getAndIncrement();
            } finally {
              latch.countDown(); // 작업 끝났다고 알림
            }
          });
    }

    latch.await(); // 100명이 다 끝날 때까지 대기

    // [Then] 검증
    String remaining = redisTemplate.opsForValue().get("inventory:available:" + productId);
    int remainingStock = remaining != null ? Integer.parseInt(remaining) : -1;

    System.out.println("-------------------------------------------");
    System.out.println("성공한 요청 수: " + successCount.get());
    System.out.println("실패한 요청 수: " + failCount.get());
    System.out.println("최종 Redis 재고: " + remainingStock);
    System.out.println("-------------------------------------------");

    assertThat(successCount.get()).isEqualTo(100);
    assertThat(failCount.get()).isEqualTo(0);
    assertThat(remainingStock).isEqualTo(0);
  }

  @Test
  @DisplayName("10000명이 랜덤 상품 + 랜덤 수량 동시 예약 테스트 (상품별 성공/실패 집계 포함)")
  void multiProductConcurrentReservationTest() throws InterruptedException {
    int threadCount = 10000;
    ExecutorService executor = Executors.newFixedThreadPool(32);
    CountDownLatch latch = new CountDownLatch(threadCount);

    Map<Long, AtomicInteger> successPerProduct = new ConcurrentHashMap<>();
    Map<Long, AtomicInteger> failPerProduct = new ConcurrentHashMap<>();
    productIds.forEach(
        id -> {
          successPerProduct.put(id, new AtomicInteger());
          failPerProduct.put(id, new AtomicInteger());
        });

    Random random = new Random();

    final long startTime = System.currentTimeMillis();

    for (int i = 0; i < threadCount; i++) {
      executor.submit(
          () -> {
            Long productId = productIds.get(random.nextInt(productIds.size()));
            int quantity = random.nextInt(3) + 1; // 1~3 랜덤 수량

            try {
              reserveUseCase.reserveInventory(
                  new InventoryReserveRequest(
                      Collections.singletonList(
                          new InventoryReserveRequest.Item(productId, quantity))));
              successPerProduct.get(productId).addAndGet(quantity);
            } catch (Exception e) {
              failPerProduct.get(productId).addAndGet(quantity);
            } finally {
              latch.countDown();
            }
          });
    }

    latch.await();
    executor.shutdown();

    long endTime = System.currentTimeMillis(); // 종료 시간
    long duration = endTime - startTime;

    System.out.println("-------------------------------------------");
    System.out.println("멀티 상품 랜덤 수량 테스트 (상품별 집계)");
    System.out.println("총 실행 시간: " + duration + " ms");

    productIds.forEach(
        id -> {
          String remaining = redisTemplate.opsForValue().get("inventory:available:" + id);
          int remainingStock = remaining != null ? Integer.parseInt(remaining) : -1;

          System.out.println("상품 " + id + ":");
          System.out.println("  성공 예약 수량: " + successPerProduct.get(id).get());
          System.out.println("  실패 수량: " + failPerProduct.get(id).get());
          System.out.println("  최종 Redis 재고: " + remainingStock);
        });

    System.out.println("-------------------------------------------");
  }
}
