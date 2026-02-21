package com.modeunsa.boundedcontext.inventory.app.command;

import com.modeunsa.boundedcontext.inventory.app.common.InventoryMapper;
import com.modeunsa.boundedcontext.inventory.domain.Inventory;
import com.modeunsa.boundedcontext.inventory.domain.InventoryProduct;
import com.modeunsa.boundedcontext.inventory.out.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryCreateInventoryUseCase {
  private final InventoryMapper inventoryMapper;
  private final InventoryRepository inventoryRepository;

  public void createInventory(InventoryProduct product) {
    try {
      Inventory inventory = inventoryMapper.toInventoryFromProduct(product);
      inventoryRepository.save(inventory);
    } catch (DataIntegrityViolationException e) {
      log.info("이미 생성된 재고입니다. productId: {}", product.getId());
    }
  }
}
