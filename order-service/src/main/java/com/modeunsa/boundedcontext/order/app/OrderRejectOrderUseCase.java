package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.Order;
import com.modeunsa.boundedcontext.order.domain.OrderMapper;
import com.modeunsa.boundedcontext.order.out.OrderRepository;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.order.event.OrderCancellationConfirmedEvent;
import com.modeunsa.shared.payment.dto.PaymentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderRejectOrderUseCase {
  private final OrderRepository orderRepository;
  private final OrderMapper orderMapper;
  private final EventPublisher eventPublisher;

  public void rejectOrder(PaymentDto payment) {
    Order order =
        orderRepository
            .findById(payment.orderId())
            .orElseThrow(() -> new GeneralException(ErrorStatus.ORDER_NOT_FOUND));

    order.reject();

    eventPublisher.publish(
        new OrderCancellationConfirmedEvent(
            order.getId(), orderMapper.toOrderItemDto(order.getOrderItems())));
  }
}
