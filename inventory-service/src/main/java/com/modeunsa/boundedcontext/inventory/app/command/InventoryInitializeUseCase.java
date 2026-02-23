package com.modeunsa.boundedcontext.inventory.app.command;

import com.modeunsa.boundedcontext.inventory.app.port.InventoryCommandPort;
import com.modeunsa.boundedcontext.inventory.domain.Inventory;
import com.modeunsa.boundedcontext.inventory.out.InventoryRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.inventory.dto.InventoryInitializeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryInitializeUseCase {
  private final InventoryRepository inventoryRepository;
  private final InventoryCommandPort inventoryCommandPort;

  public void initializeInventory(
      Long sellerId, Long productId, InventoryInitializeRequest request) {
    // 비관적 락(차감 이벤트와 동시 접근 시 대기)
    Inventory inventory =
        inventoryRepository
            .findWithLockByProductId(productId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.INVENTORY_NOT_FOUND));

    if (!inventory.getSellerId().equals(sellerId)) {
      throw new GeneralException(ErrorStatus.INVENTORY_ACCESS_DENIED);
    }

    inventory.initializeQuantity(request.quantity());

    inventoryCommandPort.initialize(productId, request.quantity());
  }
}
