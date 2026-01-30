package com.modeunsa.boundedcontext.inventory.app;

import com.modeunsa.boundedcontext.inventory.domain.Inventory;
import com.modeunsa.boundedcontext.inventory.domain.InventoryProduct;
import com.modeunsa.boundedcontext.inventory.out.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryCreateInventoryUseCase {
  private final InventoryMapper inventoryMapper;
  private final InventoryRepository inventoryRepository;

  public void createInventory(InventoryProduct product) {
    Inventory inventory = inventoryMapper.toInventoryFromProduct(product);
    inventoryRepository.save(inventory);
  }
}
