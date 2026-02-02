package com.modeunsa.boundedcontext.inventory.app;

import com.modeunsa.boundedcontext.inventory.domain.Inventory;
import com.modeunsa.boundedcontext.inventory.out.InventoryRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.inventory.dto.InventoryUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryReserveInventoryUseCase {
  private final InventoryRepository inventoryRepository;

  @Retryable(
      retryFor = ObjectOptimisticLockingFailureException.class,
      maxAttempts = 50,
      backoff = @Backoff(delay = 10) // 0.01초 대기 후 재시도
      )
  public void reserveInventory(Long productId, InventoryUpdateRequest request) {
    Inventory inventory =
        inventoryRepository
            .findByProductId(productId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.INVENTORY_NOT_FOUND));

    if (!inventory.reserve(request.quantity())) {
      throw new GeneralException(ErrorStatus.INSUFFICIENT_STOCK);
    }

    inventoryRepository.saveAndFlush(inventory);
  }
}
