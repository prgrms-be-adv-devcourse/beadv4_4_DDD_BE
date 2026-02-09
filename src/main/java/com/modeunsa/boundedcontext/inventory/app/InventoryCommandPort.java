package com.modeunsa.boundedcontext.inventory.app;

import java.util.List;

public interface InventoryCommandPort {
  void reserve(List<Long> productId, List<Integer> quantity);

  void release(Long productId, int quantity);
}
