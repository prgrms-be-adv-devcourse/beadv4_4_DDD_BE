package com.modeunsa.boundedcontext.product.in;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import com.modeunsa.boundedcontext.product.app.ProductFacade;
import com.modeunsa.shared.member.event.MemberSignupEvent;
import com.modeunsa.shared.member.event.SellerRegisteredEvent;
import com.modeunsa.shared.order.event.OrderCancelRequestEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ProductEventListener {

  private final ProductFacade productFacade;

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handleOrderCanceledEvent(OrderCancelRequestEvent event) {
    productFacade.restoreStock(event.getOrderDto());
  }

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handleMemberSignupEvent(MemberSignupEvent event) {
    productFacade.syncMember(
        event.memberId(), event.email(), event.realName(), event.phoneNumber());
  }

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handleSellerRegisteredEvent(SellerRegisteredEvent event) {
    productFacade.syncSeller(
        event.memberSellerId(), event.businessName(), event.representativeName());
  }
}
