package com.modeunsa.boundedcontext.inventory.app;

import com.modeunsa.boundedcontext.inventory.domain.Inventory;
import com.modeunsa.boundedcontext.inventory.out.InventoryRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.inventory.dto.InventoryReserveRequest;
import com.modeunsa.shared.inventory.dto.InventoryReserveRequest.Item;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
      maxAttempts = 10, // TODO: 효율적인 동시성 처리
      backoff = @Backoff(delay = 50, maxDelay = 100, random = true))
  public void reserveInventory(Long productId, InventoryReserveRequest request) {
    List<Item> items = new ArrayList<>(request.items());

    // 데드락 방지
    items.sort(Comparator.comparing(InventoryReserveRequest.Item::productId));

    for (InventoryReserveRequest.Item item : items) {
      processSingleReservation(item);
    }
  }

  private void processSingleReservation(InventoryReserveRequest.Item item) {
    Inventory inventory =
        inventoryRepository
            .findByProductId(item.productId())
            .orElseThrow(() -> new GeneralException(ErrorStatus.INVENTORY_NOT_FOUND));

    if (!inventory.reserve(item.quantity())) {
      throw new GeneralException(ErrorStatus.INSUFFICIENT_STOCK);
    }

    inventoryRepository.saveAndFlush(inventory);
  }
}
