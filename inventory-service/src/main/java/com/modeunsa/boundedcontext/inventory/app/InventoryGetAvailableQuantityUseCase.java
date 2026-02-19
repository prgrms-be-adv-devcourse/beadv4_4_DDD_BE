package com.modeunsa.boundedcontext.inventory.app;

import com.modeunsa.boundedcontext.inventory.domain.Inventory;
import com.modeunsa.boundedcontext.inventory.out.InventoryRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.inventory.dto.InventoryAvailableQuantityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryGetAvailableQuantityUseCase {
  private final InventoryRepository inventoryRepository;
  private final InventoryQueryPort inventoryQueryPort;

  public InventoryAvailableQuantityResponse getAvailableQuantity(Long productId) {
    Inventory inventory =
        inventoryRepository
            .findByProductId(productId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.INVENTORY_NOT_FOUND));

    int availableQuantity = inventoryQueryPort.getAvailableQuantity(productId);

    return new InventoryAvailableQuantityResponse(availableQuantity);
  }
}
