package com.modeunsa.boundedcontext.inventory.app;

import com.modeunsa.shared.order.dto.OrderItemDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryReleaseInventoryUseCase {

  private final InventoryCommandPort inventoryCommandPort;

  public void releaseInventory(List<OrderItemDto> orderItems) {
    for (OrderItemDto item : orderItems) {
      inventoryCommandPort.release(item.getProductId(), item.getQuantity());
    }
  }
}
