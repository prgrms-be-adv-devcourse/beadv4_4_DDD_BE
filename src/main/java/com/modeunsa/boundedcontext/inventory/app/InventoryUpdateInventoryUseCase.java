package com.modeunsa.boundedcontext.inventory.app;

import com.modeunsa.boundedcontext.inventory.domain.Inventory;
import com.modeunsa.boundedcontext.inventory.out.InventoryRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.inventory.dto.InventoryUpdateRequest;
import com.modeunsa.shared.inventory.dto.InventoryUpdateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryUpdateInventoryUseCase {
  private final InventoryMapper inventoryMapper;
  private final InventoryRepository inventoryRepository;

  public InventoryUpdateResponse updateInventory(
      Long sellerId, Long productId, InventoryUpdateRequest inventoryUpdateRequest) {
    Inventory inventory =
        inventoryRepository
            .findByProductId(productId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.INVENTORY_NOT_FOUND));

    if (inventory.getSellerId() != sellerId) {
      throw new GeneralException(ErrorStatus.INVENTORY_ACCESS_DENIED);
    }

    boolean isUpdated = inventory.updateInventory(inventoryUpdateRequest.quantity());
    if (!isUpdated) {
      throw new GeneralException(ErrorStatus.INVALID_STOCK_QUANTITY);
    }

    return inventoryMapper.toInventoryUpdateResponse(inventory);
  }
}
