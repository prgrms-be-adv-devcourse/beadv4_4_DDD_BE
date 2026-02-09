package com.modeunsa.boundedcontext.inventory.app;

public interface InventoryCommandPort {
  void reserve(Long productId, int quantity);

  void release(Long productId, int quantity);
}
