package com.modeunsa.boundedcontext.inventory.app.port;

public interface InventoryQueryPort {
  int getAvailableQuantity(Long productId);
}
