package com.modeunsa.boundedcontext.inventory.out.redis;

import com.modeunsa.boundedcontext.inventory.out.InventoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Profile("dev")
@ConditionalOnProperty(name = "inventory.redis.bootstrap", havingValue = "true")
@Component
@RequiredArgsConstructor
public class InventoryRedisInitializer {

  private final RedisTemplate<String, String> redisTemplate;
  private final InventoryRepository inventoryRepository;

  @PostConstruct
  public void init() {
    inventoryRepository
        .findAll()
        .forEach(
            inv -> {
              String key = "inventory:available:" + inv.getProductId();
              redisTemplate.opsForValue().set(key, String.valueOf(inv.getQuantity()));
            });
  }
}
