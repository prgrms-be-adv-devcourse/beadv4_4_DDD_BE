package com.modeunsa.boundedcontext.inventory.out.redis;

import java.util.List;

public class InventoryRedisKeyUtils {
  private static final String AVAILABLE_KEY_PREFIX = "inventory:available:";

  // 인스턴스화 방지
  private InventoryRedisKeyUtils() {}

  public static String makeKey(Long productId) {
    return AVAILABLE_KEY_PREFIX + productId;
  }

  public static List<String> makeKeys(List<Long> productIds) {
    return productIds.stream().map(InventoryRedisKeyUtils::makeKey).toList();
  }
}
