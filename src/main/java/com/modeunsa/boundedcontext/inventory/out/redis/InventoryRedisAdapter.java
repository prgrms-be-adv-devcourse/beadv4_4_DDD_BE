package com.modeunsa.boundedcontext.inventory.out.redis;

import com.modeunsa.boundedcontext.inventory.app.InventoryCommandPort;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryRedisAdapter implements InventoryCommandPort {
  private final RedisTemplate<String, String> redisTemplate;

  private static final String RESERVE_LUA =
      """
      local available = tonumber(redis.call("GET", KEYS[1]) or "0")
      local requestQty = tonumber(ARGV[1])
      if available < requestQty then
        return -1
      end
      redis.call("DECRBY", KEYS[1], requestQty)
      return available - requestQty
      """;

  public void reserve(Long productId, int quantity) {
    String key = inventoryKey(productId);

    Long result =
        redisTemplate.execute(
            new DefaultRedisScript<>(RESERVE_LUA, Long.class),
            List.of(key),
            String.valueOf(quantity));

    if (result == null || result < 0) {
      throw new GeneralException(ErrorStatus.INSUFFICIENT_STOCK);
    }
  }

  private String inventoryKey(Long productId) {
    return "inventory:available:" + productId;
  }
}
