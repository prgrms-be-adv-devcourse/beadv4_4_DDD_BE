package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.Order;
import com.modeunsa.boundedcontext.order.domain.OrderMapper;
import com.modeunsa.boundedcontext.order.domain.OrderPolicy;
import com.modeunsa.boundedcontext.order.out.OrderRepository;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.order.event.OrderCancellationConfirmedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderConfirmOrderCancellationUseCase {
  private final OrderRepository orderRepository;
  private final OrderPolicy orderPolicy;
  private final OrderMapper orderMapper;
  private final EventPublisher eventPublisher;

  public void confirmOrderCancellation(Long orderId) {
    // 주문 조회
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.ORDER_NOT_FOUND));

    orderPolicy.validateCancellationConfirmable(order);

    order.confirmCancellation();

    eventPublisher.publish(
        new OrderCancellationConfirmedEvent(orderMapper.toOrderItemDto(order.getOrderItems())));
  }
}
