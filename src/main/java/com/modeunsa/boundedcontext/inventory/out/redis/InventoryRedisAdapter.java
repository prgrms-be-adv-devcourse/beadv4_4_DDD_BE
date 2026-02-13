package com.modeunsa.boundedcontext.inventory.out.redis;

import com.modeunsa.boundedcontext.inventory.app.InventoryCommandPort;
import com.modeunsa.boundedcontext.inventory.app.InventoryQueryPort;
import com.modeunsa.boundedcontext.inventory.domain.Inventory;
import com.modeunsa.boundedcontext.inventory.out.InventoryRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.order.out.OrderApiClient;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryRedisAdapter implements InventoryCommandPort, InventoryQueryPort {
  private final RedisTemplate<String, String> redisTemplate;
  private final InventoryRepository inventoryRepository;
  private final OrderApiClient orderApiClient;

  private static final String RESERVE_MULTI_LUA =
      """
      for i=1,#KEYS do
          local available = tonumber(redis.call("GET", KEYS[i]) or "0")
          local requestQty = tonumber(ARGV[i])
          if available < requestQty then
              return -1
          end
      end
      for i=1,#KEYS do
          redis.call("DECRBY", KEYS[i], ARGV[i])
      end
      return 1
      """;

  private static final String RELEASE_LUA =
      """
      redis.call("INCRBY", KEYS[1], ARGV[1])
      return 1
      """;

  public void reserve(List<Long> productIds, List<Integer> quantities) {
    List<String> keys = productIds.stream().map(id -> "inventory:available:" + id).toList();
    List<String> args = quantities.stream().map(String::valueOf).toList();

    Long result =
        redisTemplate.execute(
            new DefaultRedisScript<>(RESERVE_MULTI_LUA, Long.class),
            keys,
            args.toArray(new String[0]));

    if (result == null || result < 0) {
      throw new GeneralException(ErrorStatus.INSUFFICIENT_STOCK);
    }
  }

  public void release(Long productId, int quantity) {
    redisTemplate.execute(
        new DefaultRedisScript<>(RELEASE_LUA, Long.class),
        List.of(inventoryKey(productId)),
        String.valueOf(quantity));
  }

  @Override
  public int getAvailableQuantity(Long productId) {
    String key = inventoryKey(productId);
    String value = redisTemplate.opsForValue().get(key);

    // Redis에 값이 있으면 그대로 반환
    if (value != null) {
      return Integer.parseInt(value);
    }

    //  Redis에 값이 없을 때(만료되었거나 최초 조회 시)
    // DB에서 조회하여 Redis에 채워넣고 반환해야 함
    return refreshRedisFromDb(productId, key);
  }

  private String inventoryKey(Long productId) {
    return "inventory:available:" + productId;
  }

  private int refreshRedisFromDb(Long productId, String key) {
    // DB에서 계산
    Inventory inventory =
        inventoryRepository
            .findById(productId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.INVENTORY_NOT_FOUND));
    int pendingQuantity = 0;
    try {
      pendingQuantity = orderApiClient.getPendingCount(productId);
    } catch (Exception e) {
      throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
    }

    int availableQuantity = Math.max(inventory.getStockQuantity() - pendingQuantity, 0);

    // Redis에 저장 시도 (SETNX)
    // redis에 키 있으면 데이터 버리기
    Boolean isSet =
        redisTemplate
            .opsForValue()
            .setIfAbsent(key, String.valueOf(availableQuantity), Duration.ofMinutes(5));

    // 이미 키가 있을 때
    if (Boolean.FALSE.equals(isSet)) {
      // Redis에 있는 최신 값을 가져와서 반환
      String recentValue = redisTemplate.opsForValue().get(key);
      return recentValue != null ? Integer.parseInt(recentValue) : availableQuantity;
    }

    return availableQuantity;
  }
}
