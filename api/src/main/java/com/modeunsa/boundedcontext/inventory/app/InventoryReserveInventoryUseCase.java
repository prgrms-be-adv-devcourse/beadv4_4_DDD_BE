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

    // productId 순으로 정렬 (선택사항)
    List<InventoryReserveRequest.Item> items =
        request.items().stream()
            .sorted(Comparator.comparing(InventoryReserveRequest.Item::productId))
            .toList();

    // 멀티 상품용 Lua에 넘길 key/value 준비
    List<Long> productIds = items.stream().map(InventoryReserveRequest.Item::productId).toList();
    List<Integer> quantities = items.stream().map(InventoryReserveRequest.Item::quantity).toList();

    // 멀티 상품 Lua 호출
    inventoryCommandPort.reserve(productIds, quantities);
  }
}
