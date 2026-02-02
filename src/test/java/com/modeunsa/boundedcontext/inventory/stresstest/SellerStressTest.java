package com.modeunsa.boundedcontext.inventory.stresstest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.modeunsa.boundedcontext.inventory.app.InventoryUpdateInventoryUseCase;
import com.modeunsa.boundedcontext.inventory.domain.Inventory;
import com.modeunsa.boundedcontext.inventory.out.InventoryRepository;
import com.modeunsa.shared.inventory.dto.InventoryUpdateRequest;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;

@Tag("ignore")
@SpringBootTest
@ActiveProfiles("test")
public class SellerStressTest {

  @Autowired private InventoryRepository inventoryRepository;

  @Autowired private InventoryUpdateInventoryUseCase sellerService; // 판매자 서비스 (Retry 적용됨)

  @Autowired private TempCustomerService tempCustomerService; // 임시 고객 서비스

  @BeforeEach
  void cleanUp() {
    inventoryRepository.deleteAll();
  }

  // ---------------------------------------------------------
  // 테스트용 임시 고객 서비스 (아까와 동일)
  // ---------------------------------------------------------
  @TestConfiguration
  @EnableRetry
  static class TestConfig {
    @Bean
    public TempCustomerService tempCustomerService(InventoryRepository inventoryRepository) {
      return new TempCustomerService(inventoryRepository);
    }
  }

  static class TempCustomerService {
    private final InventoryRepository inventoryRepository;

    public TempCustomerService(InventoryRepository inventoryRepository) {
      this.inventoryRepository = inventoryRepository;
    }

    @org.springframework.retry.annotation.Retryable(
        retryFor = ObjectOptimisticLockingFailureException.class,
        maxAttempts = 100,
        backoff = @org.springframework.retry.annotation.Backoff(delay = 10))
    @org.springframework.transaction.annotation.Transactional
    public void reserve(Long productId) {
      Inventory inventory = inventoryRepository.findByProductId(productId).orElseThrow();
      // 고객은 예약 수량만 늘림
      inventory.setReservedQuantity(inventory.getReservedQuantity() + 1);
      inventoryRepository.save(inventory);
    }
  }

  // ---------------------------------------------------------

  @Test
  @DisplayName("고객 50명이 예약을 거는 와중에, 사장님이 재고 수정을 시도하면 둘 다 정상 반영되어야 한다.")
  void seller_update_under_pressure() throws InterruptedException {
    // [Given] 초기 세팅 (전체 100, 예약 0)
    Inventory inventory =
        inventoryRepository.saveAndFlush(
            Inventory.builder()
                .sellerId(1L)
                .productId(1L)
                .quantity(100)
                .reservedQuantity(0)
                .build());
    Long productId = inventory.getProductId();
    Long sellerId = inventory.getSellerId();

    // [When]
    int customerCount = 50; // 고객 50명
    ExecutorService executorService = Executors.newFixedThreadPool(32);
    CountDownLatch latch = new CountDownLatch(customerCount + 1); // 고객 50명 + 사장 1명

    // (1) 고객 50명 투입
    for (int i = 0; i < customerCount; i++) {
      executorService.submit(
          () -> {
            try {
              tempCustomerService.reserve(productId);
            } catch (Exception e) {
              System.out.println("고객 실패: " + e.getMessage());
            } finally {
              latch.countDown();
            }
          });
    }

    // (2) 사장님 투입
    executorService.submit(
        () -> {
          try {
            // 고객들이 DB를 마구 수정하는 중에 사장님이 들어감 -> 충돌 발생 -> Retry 동작
            System.out.println("👨‍💼 사장님: 재고 1000개로 수정 시도!");
            sellerService.updateInventory(sellerId, productId, new InventoryUpdateRequest(1000));
            System.out.println("👨‍💼 사장님: 수정 성공!");
          } catch (Exception e) {
            System.out.println("❌ 사장님 최종 실패: " + e.getMessage());
          } finally {
            latch.countDown();
          }
        });

    latch.await();

    // 3. [Then] 검증
    Inventory finalInventory = inventoryRepository.findById(inventory.getId()).orElseThrow();

    System.out.println("-----------------------------------------");
    System.out.println("최종 전체 재고: " + finalInventory.getQuantity());
    System.out.println("최종 예약 재고: " + finalInventory.getReservedQuantity());
    System.out.println("-----------------------------------------");

    // 검증 1: 사장님의 의도대로 전체 재고가 1000개가 되었는지
    assertThat(finalInventory.getQuantity()).isEqualTo(1000);

    // 검증 2: 사장님이 수정하면서, 고객들의 예약을 날려먹지 않았는지
    assertThat(finalInventory.getReservedQuantity()).isEqualTo(50);
  }
}
