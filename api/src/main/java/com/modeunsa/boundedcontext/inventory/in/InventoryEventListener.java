package com.modeunsa.boundedcontext.inventory.in;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import com.modeunsa.boundedcontext.inventory.app.InventoryFacade;
import com.modeunsa.shared.inventory.event.InventoryStockRecoverEvent;
import com.modeunsa.shared.member.event.SellerRegisteredEvent;
import com.modeunsa.shared.order.event.OrderCancellationConfirmedEvent;
import com.modeunsa.shared.order.event.OrderPaidEvent;
import com.modeunsa.shared.product.event.ProductCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class InventoryEventListener {

  private final InventoryFacade inventoryFacade;

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handleSellerRegisteredEvent(SellerRegisteredEvent event) {
    inventoryFacade.registerSeller(
        event.memberSellerId(), event.businessName(), event.representativeName());
  }

  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handle(ProductCreatedEvent event) {
    inventoryFacade.createProduct(event.productDto());
  }

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handle(OrderCancellationConfirmedEvent event) {
    inventoryFacade.releaseInventory(event.orderItemDto());
  }

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handle(OrderPaidEvent event) {
    inventoryFacade.decreaseStock(event.orderDto().getOrderItems());
  }

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handle(InventoryStockRecoverEvent event) {
    inventoryFacade.increaseStock(event.orderItems());
  }
}
