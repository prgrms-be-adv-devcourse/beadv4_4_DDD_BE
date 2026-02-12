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

  private String inventoryKey(Long productId) {
    return "inventory:available:" + productId;
  }
}
