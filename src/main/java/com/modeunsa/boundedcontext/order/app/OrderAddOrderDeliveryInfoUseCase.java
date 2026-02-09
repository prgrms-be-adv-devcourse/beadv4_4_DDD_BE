package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.Order;
import com.modeunsa.boundedcontext.order.out.OrderRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.order.OrderDeliveryRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderAddOrderDeliveryInfoUseCase {
  private final OrderRepository orderRepository;

  public void addOrderDeliveryInfo(Long memberId, Long orderId, OrderDeliveryRequestDto request) {
    Order order =
        orderRepository
            .findByIdAndOrderMemberId(orderId, memberId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.ORDER_NOT_FOUND));

    order.addDeliveryInfo(
        request.recipientName(),
        request.recipientPhone(),
        request.zipCode(),
        request.address(),
        request.addressDetail());
  }
}
