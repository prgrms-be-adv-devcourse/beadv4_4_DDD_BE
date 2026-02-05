package com.modeunsa.boundedcontext.inventory.app;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.modeunsa.shared.inventory.dto.InventoryReserveRequest;
import com.modeunsa.shared.inventory.dto.InventoryReserveRequest.Item;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
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
public class InventoryReserveInventoryUseCaseTest {

  @Autowired private InventoryReserveInventoryUseCase reserveUseCase;

  @Autowired private RedisTemplate<String, String> redisTemplate;

  private static final Long PRODUCT_ID = 1L;

  @BeforeEach
  void setUp() {
    // [Given] 재고 1000개 세팅 (예약 0개)
    redisTemplate.opsForValue().set("inventory:available:" + PRODUCT_ID, "100");
  }

  @AfterEach
  void tearDown() {
    // Redis 초기화
    redisTemplate.delete("inventory:available:" + PRODUCT_ID);
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

    for (int i = 0; i < threadCount; i++) {
      executorService.submit(
          () -> {
            try {
              // 동시에 예약 요청
              reserveUseCase.reserveInventory(
                  new InventoryReserveRequest(Collections.singletonList(new Item(1L, 1))));
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
    String remaining = redisTemplate.opsForValue().get("inventory:available:" + PRODUCT_ID);
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
}
