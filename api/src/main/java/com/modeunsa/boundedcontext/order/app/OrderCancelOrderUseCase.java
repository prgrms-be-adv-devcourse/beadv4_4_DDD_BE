package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.Order;
import com.modeunsa.boundedcontext.order.domain.OrderMapper;
import com.modeunsa.boundedcontext.order.domain.OrderPolicy;
import com.modeunsa.boundedcontext.order.out.OrderRepository;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.order.dto.OrderResponseDto;
import com.modeunsa.shared.order.event.RefundRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCancelOrderUseCase {
  private final OrderRepository orderRepository;
  private final EventPublisher eventPublisher;
  private final OrderMapper orderMapper;
  private final OrderPolicy orderPolicy;

  public OrderResponseDto cancelOrder(Long memberId, Long orderId) {
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
    orderPolicy.validateCancellable(order);

    order.requestCancel();

    eventPublisher.publish(new RefundRequestedEvent(orderMapper.toOrderDto(order)));

    return orderMapper.toOrderResponseDto(order);
  }
}
