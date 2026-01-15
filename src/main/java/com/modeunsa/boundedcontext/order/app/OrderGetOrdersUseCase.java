package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.Order;
import com.modeunsa.boundedcontext.order.domain.OrderMapper;
import com.modeunsa.boundedcontext.order.out.OrderMemberRepository;
import com.modeunsa.boundedcontext.order.out.OrderRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.order.dto.OrderListResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrderGetOrdersUseCase {
  private final OrderMemberRepository orderMemberRepository;
  private final OrderRepository orderRepository;
  private final OrderMapper orderMapper;

  public Page<OrderListResponseDto> getOrders(long memberId, Pageable pageable) {
    // 회원 확인
    if (!orderMemberRepository.existsById(memberId)) {
      throw new GeneralException(ErrorStatus.ORDER_MEMBER_NOT_FOUND);
    }

    Page<Order> orderPage = orderRepository.findAllByOrderMemberId(memberId, pageable);

    return orderPage.map(orderMapper::toOrderListResponseDto);
  }
}
