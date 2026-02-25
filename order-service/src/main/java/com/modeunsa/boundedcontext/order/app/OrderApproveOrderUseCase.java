package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.Order;
import com.modeunsa.boundedcontext.order.domain.OrderMapper;
import com.modeunsa.boundedcontext.order.out.OrderRepository;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.order.event.OrderPaidEvent;
import com.modeunsa.shared.payment.dto.PaymentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderApproveOrderUseCase {
  private final OrderRepository orderRepository;
  private final EventPublisher eventPublisher;
  private final OrderMapper orderMapper;

  public void approveOrder(PaymentDto payment) {
    Order order =
        orderRepository
            .findById(payment.orderId())
            .orElseThrow(() -> new GeneralException(ErrorStatus.ORDER_NOT_FOUND));

    order.approve();

    eventPublisher.publish(new OrderPaidEvent(orderMapper.toOrderDto(order)));
  }
}
