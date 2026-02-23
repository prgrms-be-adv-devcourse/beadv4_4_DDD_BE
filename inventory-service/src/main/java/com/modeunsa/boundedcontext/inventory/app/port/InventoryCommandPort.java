package com.modeunsa.boundedcontext.inventory.app.port;

import java.util.List;

public interface InventoryCommandPort {
  void initialize(Long productId, int quantity);

  void reserve(List<Long> productId, List<Integer> quantity);

  void release(Long productId, int quantity);
}
