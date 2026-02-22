package com.modeunsa.boundedcontext.inventory.out.redis;

import com.modeunsa.boundedcontext.inventory.app.port.InventoryCommandPort;
import com.modeunsa.boundedcontext.inventory.app.port.InventoryQueryPort;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryRedisAdapter implements InventoryCommandPort, InventoryQueryPort {
  private final RedisTemplate<String, String> redisTemplate;

  @Qualifier("reserveInventoryScript")
  private final RedisScript<Long> reserveInventoryScript;

  @Qualifier("releaseInventoryScript")
  private final RedisScript<Long> releaseInventoryScript;

  private static final String INVENTORY_KEY_PREFIX = "inventory:available:";

  @Override
  public void reserve(List<Long> productIds, List<Integer> quantities) {
    List<String> keys = productIds.stream().map(id -> INVENTORY_KEY_PREFIX + id).toList();
    List<String> args = quantities.stream().map(String::valueOf).toList();

    Long result = redisTemplate.execute(reserveInventoryScript, keys, args.toArray(new String[0]));

    if (result == null || result < 0) {
      throw new GeneralException(ErrorStatus.INSUFFICIENT_STOCK);
    }
  }

  @Override
  public void release(Long productId, int quantity) {
    redisTemplate.execute(
        releaseInventoryScript,
        List.of(InventoryRedisKeyUtils.makeKey(productId)),
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
    return INVENTORY_KEY_PREFIX + productId;
  }

  private int refreshRedisFromDb(Long productId, String key) {
    // TODO: 양방향참조 삭제

    int availableQuantity = 0;

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
