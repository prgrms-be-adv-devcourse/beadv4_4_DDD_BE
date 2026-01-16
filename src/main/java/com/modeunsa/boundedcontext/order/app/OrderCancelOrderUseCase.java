package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.Order;
import com.modeunsa.boundedcontext.order.domain.OrderMapper;
import com.modeunsa.boundedcontext.order.out.OrderRepository;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.order.dto.OrderResponseDto;
import com.modeunsa.shared.order.event.OrderCancelRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCancelOrderUseCase {
  private final OrderRepository orderRepository;
  private final SpringDomainEventPublisher eventPublisher;
  private final OrderMapper orderMapper;

  public OrderResponseDto cancelOrder(long memberId, Long orderId) {
    // 주문 확인
    Order order =
        orderRepository
            .findByIdWithFetch(orderId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.ORDER_NOT_FOUND));

    // 권한 확인
    if (!order.getOrderMember().getId().equals(memberId)) {
      throw new GeneralException(ErrorStatus.ORDER_ACCESS_DENIED);
    }

    // 배송전 단계에서만 주문 취소 가능
    if (!order.isCancellable()) {
      throw new GeneralException(ErrorStatus.ORDER_CANNOT_CANCEL);
    }

    order.requestCancel();

    eventPublisher.publish(new OrderCancelRequestEvent(orderMapper.toOrderDto(order)));

    return orderMapper.toOrderResponseDto(order);
  }
}
