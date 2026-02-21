package com.modeunsa.boundedcontext.inventory.app.command;

import com.modeunsa.boundedcontext.inventory.app.port.InventoryCommandPort;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.shared.inventory.event.InventoryStockRecoverEvent;
import com.modeunsa.shared.order.dto.OrderItemDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryReleaseInventoryUseCase {

  private final InventoryCommandPort inventoryCommandPort;
  private final EventPublisher eventPublisher;

  public void releaseInventory(List<OrderItemDto> orderItems) {
    for (OrderItemDto item : orderItems) {
      inventoryCommandPort.release(item.getProductId(), item.getQuantity());
    }

    eventPublisher.publish(new InventoryStockRecoverEvent(orderItems));
  }
}
