package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.Order;
import com.modeunsa.boundedcontext.order.domain.OrderMapper;
import com.modeunsa.boundedcontext.order.out.OrderMemberRepository;
import com.modeunsa.boundedcontext.order.out.OrderRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.order.dto.OrderListResponseDto;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrderGetOrdersUseCase {
  private final OrderMemberRepository orderMemberRepository;
  private final OrderRepository orderRepository;
  private final OrderMapper orderMapper;

  public List<OrderListResponseDto> getOrders(long memberId) {
    // 회원 확인
    if (!orderMemberRepository.existsById(memberId)) {
      throw new GeneralException(ErrorStatus.ORDER_MEMBER_NOT_FOUND);
    }

    List<Order> orders = orderRepository.findAllByOrderMemberIdOrderByCreatedAtDesc(memberId);

    return orderMapper.toOrderListResponseDtos(orders);
  }
}
