package com.modeunsa.boundedcontext.order.in;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import com.modeunsa.boundedcontext.order.app.OrderFacade;
import com.modeunsa.shared.auth.event.MemberSignupEvent;
import com.modeunsa.shared.member.event.MemberBasicInfoUpdatedEvent;
import com.modeunsa.shared.member.event.MemberDeliveryAddressSetAsDefaultEvent;
import com.modeunsa.shared.payment.event.PaymentFailedEvent;
import com.modeunsa.shared.payment.event.PaymentSuccessEvent;
import com.modeunsa.shared.product.event.ProductCreatedEvent;
import com.modeunsa.shared.product.event.ProductUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderEventListener {
  private final OrderFacade orderFacade;

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handle(MemberSignupEvent event) {
    orderFacade.syncMember(event.memberId(), event.realName(), event.phoneNumber());
  }

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handle(MemberBasicInfoUpdatedEvent event) {
    orderFacade.updateMember(event.memberId(), event.realName(), event.phoneNumber());
  }

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handle(MemberDeliveryAddressSetAsDefaultEvent event) {
    orderFacade.createDeliveryAddress(
        event.memberId(), event.zipCode(), event.address(), event.addressDetail());
  }

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handle(ProductCreatedEvent event) {
    orderFacade.createProduct(event.productDto());
  }

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handle(ProductUpdatedEvent event) {
    orderFacade.updateProduct(event.productDto());
  }

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handle(PaymentSuccessEvent event) {
    orderFacade.approveOrder(event.payment());
  }

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handle(PaymentFailedEvent event) {
    orderFacade.rejectOrder(event.payment());
  }
}
