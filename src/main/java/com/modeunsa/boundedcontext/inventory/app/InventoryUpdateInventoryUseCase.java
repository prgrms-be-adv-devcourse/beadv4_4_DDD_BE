package com.modeunsa.boundedcontext.inventory.app;

import com.modeunsa.boundedcontext.inventory.domain.Inventory;
import com.modeunsa.boundedcontext.inventory.out.InventoryRepository;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.inventory.dto.InventoryUpdateRequest;
import com.modeunsa.shared.inventory.dto.InventoryUpdateResponse;
import com.modeunsa.shared.inventory.event.ProductSoldOutEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryUpdateInventoryUseCase {
  private final InventoryMapper inventoryMapper;
  private final InventoryRepository inventoryRepository;
  private final EventPublisher eventPublisher;

  @Retryable(
      retryFor = ObjectOptimisticLockingFailureException.class,
      maxAttempts = 3,
      backoff = @Backoff(delay = 50))
  public InventoryUpdateResponse updateInventory(
      Long sellerId, Long productId, InventoryUpdateRequest inventoryUpdateRequest) {
    Inventory inventory =
        inventoryRepository
            .findByProductId(productId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.INVENTORY_NOT_FOUND));

    if (!inventory.isOwner(sellerId)) {
      throw new GeneralException(ErrorStatus.INVENTORY_ACCESS_DENIED);
    }

    boolean isUpdated = inventory.updateInventory(inventoryUpdateRequest.quantity());
    if (!isUpdated) {
      throw new GeneralException(ErrorStatus.INVALID_STOCK_QUANTITY);
    }
    inventoryRepository.saveAndFlush(inventory);

    if (inventory.getQuantity() == 0) {
      eventPublisher.publish(new ProductSoldOutEvent(productId));
    }

    return inventoryMapper.toInventoryUpdateResponse(inventory);
  }
}
