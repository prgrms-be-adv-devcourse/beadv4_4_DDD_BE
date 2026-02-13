package com.modeunsa.boundedcontext.inventory.app;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.modeunsa.boundedcontext.inventory.domain.Inventory;
import com.modeunsa.boundedcontext.inventory.out.InventoryRepository;
import com.modeunsa.shared.inventory.dto.InventoryReserveRequest;
import com.modeunsa.shared.inventory.dto.InventoryReserveRequest.Item;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Tag("ignore")
@SpringBootTest
@ActiveProfiles("test")
public class InventoryReserveInventoryUseCaseTest {

  @Autowired private InventoryReserveInventoryUseCase reserveUseCase;

  @Autowired private InventoryRepository inventoryRepository;

  @AfterEach
  void tearDown() {
    inventoryRepository.deleteAll();
  }

  @Test
  @DisplayName("100명이 동시에 1개씩 주문(예약)하면, Retry 덕분에 정확히 100개가 예약되어야 한다.")
  void concurrentReservationTest() throws InterruptedException {
    // [Given] 재고 1000개 세팅 (예약 0개)
    Inventory inventory =
        inventoryRepository.saveAndFlush(
            Inventory.builder()
                .sellerId(1L)
                .productId(1L)
                .quantity(1000)
                .reservedQuantity(0)
                .build());
    Long productId = inventory.getProductId();

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
    Inventory finalInventory = inventoryRepository.findById(inventory.getId()).orElseThrow();

    System.out.println("-------------------------------------------");
    System.out.println("성공한 요청 수: " + successCount.get());
    System.out.println("실패한 요청 수: " + failCount.get());
    System.out.println("최종 DB 예약 수량: " + finalInventory.getReservedQuantity());
    System.out.println("-------------------------------------------");

    // 검증 1: 100번 시도했으니 100번 다 성공했어야 함
    assertThat(successCount.get()).isEqualTo(100);

    // 검증 2: 실제 DB에도 예약 수량이 100개여야 함
    assertThat(finalInventory.getReservedQuantity()).isEqualTo(100);
  }
}
