package com.modeunsa.boundedcontext.inventory.app;

import com.modeunsa.shared.inventory.dto.InventoryReserveRequest;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryReserveInventoryUseCase {
  private final InventoryCommandPort inventoryCommandPort;

  public void reserveInventory(InventoryReserveRequest request) {
    List<InventoryReserveRequest.Item> items =
        request.items().stream()
            .sorted(Comparator.comparing(InventoryReserveRequest.Item::productId))
            .toList();

    for (InventoryReserveRequest.Item item : items) {
      inventoryCommandPort.reserve(item.productId(), item.quantity());
    }
  }
}
