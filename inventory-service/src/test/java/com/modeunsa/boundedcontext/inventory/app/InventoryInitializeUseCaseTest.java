package com.modeunsa.boundedcontext.inventory.app;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.modeunsa.boundedcontext.inventory.app.command.InventoryInitializeUseCase;
import com.modeunsa.boundedcontext.inventory.domain.Inventory;
import com.modeunsa.boundedcontext.inventory.out.InventoryRepository;
import com.modeunsa.shared.inventory.dto.InventoryInitializeRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@Tag("ignore")
@SpringBootTest
@ActiveProfiles("test")
public class InventoryInitializeUseCaseTest {
  @Autowired private InventoryInitializeUseCase inventoryInitializeUseCase;
  @Autowired private InventoryRepository inventoryRepository;
  @Autowired private StringRedisTemplate redisTemplate;

  // 테스트가 끝날 때마다 Redis 데이터를 지워주어 다음 테스트에 영향을 주지 않도록 합니다.
  @AfterEach
  void tearDown() {
    // 테스트에 사용된 키를 직접 지우거나, 전체를 초기화합니다.
    redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
  }

  @Test
  @DisplayName("재고 초기화 메서드를 호출하면, 실제 Redis에 지정된 키와 값으로 데이터가 저장된다")
  void setInitialStock_savesToRealRedis() {
    // given
    Long productId = 1L;
    String expectedKey = "inventory:available:" + productId;

    // (수량 0, 초기화 안 됨)
    Inventory emptyInventory =
        Inventory.builder()
            .productId(productId)
            .sellerId(1L)
            .quantity(0)
            .isInitialized(false)
            .build();

    inventoryRepository.save(emptyInventory);

    // when:
    inventoryInitializeUseCase.initializeInventory(productId, new InventoryInitializeRequest(100));

    // then:
    String savedQuantity = redisTemplate.opsForValue().get(expectedKey);

    // 값이 존재하는지
    assertThat(savedQuantity).isNotNull();
    // 값이 맞는지
    assertThat(savedQuantity).isEqualTo("100");
  }
}
