package com.modeunsa.boundedcontext.inventory.app;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;

import com.modeunsa.boundedcontext.inventory.domain.Inventory;
import com.modeunsa.boundedcontext.inventory.out.InventoryRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.shared.inventory.dto.InventoryUpdateRequest;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
@ActiveProfiles("test")
public class InventoryUpdateInventoryUseCaseTest {

  @Autowired private InventoryRepository inventoryRepository;

  @Autowired private InventoryUpdateInventoryUseCase sellerService; // 판매자 (Retry 적용됨)

  @MockitoSpyBean private InventoryRepository inventoryRepositorySpy;

  @Autowired private EntityManager entityManager;

  @BeforeEach
  void cleanUp() {
    inventoryRepository.deleteAll();
  }

  @Test
  @DisplayName("사장이 조회 후 커밋하기 직전에, 고객이 예약을 걸면 -> 낙관적 락 발생 -> 재시도 -> 비즈니스 예외 발생")
  void race_condition_test() throws InterruptedException, ExecutionException {
    // [Given] 데이터 세팅 (재고 10, 예약 0)
    Inventory inventory =
        inventoryRepository.saveAndFlush(
            Inventory.builder()
                .sellerId(1L)
                .productId(1L)
                .quantity(10)
                .reservedQuantity(0)
                .build());
    Long productId = inventory.getProductId();
    Long sellerId = inventory.getSellerId();

    // 스레드 조율을 위한 신호등 (Latch)
    CountDownLatch sellerReadLatch = new CountDownLatch(1); // 사장이 조회했는지 확인
    CountDownLatch customerUpdateLatch = new CountDownLatch(1); // 고객이 수정을 마쳤는지 확인

    // [Interception] 사장이 조회하면 잠시 붙잡아두기
    doAnswer(
            invocation -> {
              System.out.println("👨‍💼 사장님: 조회 요청 진입");
              sellerReadLatch.countDown();

              System.out.println("👨‍💼 사장님: 잠시 대기 중... (고객이 수정하길 기다림)");
              customerUpdateLatch.await();

              System.out.println("👨‍💼 사장님: 다시 작업 재개! (이제 DB 조회함)");

              // 수동으로 쿼리를 날려서 진짜 데이터(버전 1)를 가져와서 리턴해줌.
              String query = "select i from Inventory i where i.productId = :productId";
              Inventory realResult =
                  entityManager
                      .createQuery(query, Inventory.class)
                      .setParameter("productId", productId)
                      .getSingleResult();

              return Optional.ofNullable(realResult);
            })
        .when(inventoryRepositorySpy)
        .findByProductId(productId);

    final CompletableFuture<Void> sellerTask =
        CompletableFuture.runAsync(
            () -> {
              try {
                // 사장님은 5개로 줄이려고 시도 (여기서 Spy가 동작해서 중간에 멈춤)
                sellerService.updateInventory(sellerId, productId, new InventoryUpdateRequest(5));
              } catch (Exception e) {
                System.out.println("🏁 사장님 최종 결과: " + e.getMessage());
                throw e;
              }
            });

    // [Intervene] 고객의 침투
    // 사장님이 조회를 마칠 때까지 기다림
    sellerReadLatch.await();

    // 사장님이 멈춰있는 동안 DB 수정 (버전 1 -> 2)
    System.out.println("⚡ 고객: 틈새 시장 공략! (6개 예약)");
    Inventory target = inventoryRepository.findById(inventory.getId()).get();
    target.setReservedQuantity(6); // 예약 6개로 변경
    inventoryRepository.saveAndFlush(target);

    customerUpdateLatch.countDown();

    // [Then] 결과 검증
    // 사장님 로직이 끝나기를 기다림
    assertThatThrownBy(() -> sellerTask.get())
        .isInstanceOf(ExecutionException.class)
        .cause()
        .isInstanceOf(GeneralException.class);

    // 최종 데이터 확인 (사장님 요청 무시되고, 고객 예약만 남아야 함)
    Inventory finalInv = inventoryRepository.findById(inventory.getId()).get();
    System.out.println(
        "최종 재고: " + finalInv.getQuantity() + ", 예약: " + finalInv.getReservedQuantity());

    Assertions.assertThat(finalInv.getQuantity()).isEqualTo(10); // 5로 안 바뀌고 10 유지
    Assertions.assertThat(finalInv.getReservedQuantity()).isEqualTo(6);
  }
}
