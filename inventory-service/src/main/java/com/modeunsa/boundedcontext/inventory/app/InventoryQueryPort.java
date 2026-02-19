package com.modeunsa.boundedcontext.inventory.app;

public interface InventoryQueryPort {
  int getAvailableQuantity(Long productId);
}
