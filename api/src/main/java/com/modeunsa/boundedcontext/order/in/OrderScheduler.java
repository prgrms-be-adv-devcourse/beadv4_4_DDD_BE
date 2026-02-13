package com.modeunsa.boundedcontext.order.in;

import com.modeunsa.boundedcontext.order.domain.Order;
import com.modeunsa.boundedcontext.order.domain.OrderMapper;
import com.modeunsa.boundedcontext.order.domain.OrderStatus;
import com.modeunsa.boundedcontext.order.out.OrderRepository;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.shared.order.event.OrderPurchaseConfirmedEvent;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.simulation.enabled", havingValue = "true")
@Slf4j
public class OrderScheduler {

  private final OrderRepository orderRepository;
  private final EventPublisher eventPublisher;
  private final OrderMapper orderMapper;

  // 1분마다 실행
  @Scheduled(fixedDelay = 60000)
  @Transactional
  public void autoUpdateOrderStatus() {
    LocalDateTime tenSecondsAgo = LocalDateTime.now().minusSeconds(10);

    // [결제 완료 -> 배송 완료] 처리
    List<Order> paidOrders =
        orderRepository.findAllByStatusAndPaidAtBefore(OrderStatus.PAID, tenSecondsAgo);

    for (Order order : paidOrders) {
      order.deliveryComplete(); // 배송완료
      log.info("주문[{}] 자동 배송 완료 처리", order.getId());
    }

    // [배송 완료 -> 구매 확정] 처리
    List<Order> deliveredOrders =
        orderRepository.findAllByStatusAndPaidAtBefore(OrderStatus.DELIVERED, tenSecondsAgo);

    for (Order order : deliveredOrders) {
      order.confirm(); // 구매확정
      log.info("주문[{}] 자동 구매 확정 처리", order.getId());

      eventPublisher.publish(new OrderPurchaseConfirmedEvent(orderMapper.toOrderDto(order)));
    }
  }
}
