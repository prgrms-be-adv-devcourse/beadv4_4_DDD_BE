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
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderRefundOrderUseCase {
  private final OrderRepository orderRepository;
  private final OrderMapper orderMapper;
  private final EventPublisher eventPublisher;
  private final OrderPolicy orderPolicy;

  public OrderResponseDto refundOrder(Long memberId, Long orderId) {
    // 주문 조회
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.ORDER_NOT_FOUND));

    // 주문자 본인 확인
    if (!order.getOrderMember().getId().equals(memberId)) {
      throw new GeneralException(ErrorStatus.ORDER_ACCESS_DENIED);
    }

    // 환불 요청
    orderPolicy.validateRefundable(order, LocalDateTime.now());

    // 통과했으면 상태 변경
    order.requestRefund();

    // 환불 요청 이벤트 발행
    eventPublisher.publish(new RefundRequestedEvent(orderMapper.toOrderDto(order)));

    return orderMapper.toOrderResponseDto(order);
  }
}
