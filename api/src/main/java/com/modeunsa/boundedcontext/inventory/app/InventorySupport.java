package com.modeunsa.boundedcontext.inventory.app;

import com.modeunsa.boundedcontext.inventory.domain.Inventory;
import com.modeunsa.boundedcontext.inventory.out.InventoryRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventorySupport {
  private final InventoryRepository inventoryRepository;

  public Inventory getInventory(Long productId) {
    return inventoryRepository
        .findByProductId(productId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.INVENTORY_NOT_FOUND));
  }
}
