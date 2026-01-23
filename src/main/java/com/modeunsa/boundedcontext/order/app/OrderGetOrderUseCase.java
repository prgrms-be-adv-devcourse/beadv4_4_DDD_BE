package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.Order;
import com.modeunsa.boundedcontext.order.domain.OrderMapper;
import com.modeunsa.boundedcontext.order.out.OrderRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.order.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderGetOrderUseCase {
  private final OrderRepository orderRepository;
  private final OrderMapper orderMapper;

  public OrderDto getOrder(Long memberId, Long orderId) {
    Order order =
        orderRepository
            .findByIdWithFetch(orderId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.ORDER_NOT_FOUND));

    if (!order.getOrderMember().getId().equals(memberId)) {
      throw new GeneralException(ErrorStatus.ORDER_ACCESS_DENIED);
    }

    return orderMapper.toOrderDto(order);
  }
}
