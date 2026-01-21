package com.modeunsa.boundedcontext.product.in;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import com.modeunsa.boundedcontext.product.app.ProductFacade;
import com.modeunsa.shared.order.event.OrderCancelRequestEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ProductEventListener {

  private final ProductFacade productFacade;

  // TODO: seller, member 생성 및 수정 시 event 핸들링

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handleOrderCreatedEvent(OrderCancelRequestEvent event) {
    productFacade.restoreStock(event.getOrderDto());
  }
}
