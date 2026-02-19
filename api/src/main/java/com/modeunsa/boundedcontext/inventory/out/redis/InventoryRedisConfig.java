package com.modeunsa.boundedcontext.inventory.out.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class InventoryRedisConfig {

  @Bean
  public RedisScript<Long> reserveInventoryScript() {
    return RedisScript.of(new ClassPathResource("redis/inventory_reserve.lua"), Long.class);
  }

  @Bean
  public RedisScript<Long> releaseInventoryScript() {
    return RedisScript.of(new ClassPathResource("redis/inventory_release.lua"), Long.class);
  }
}
