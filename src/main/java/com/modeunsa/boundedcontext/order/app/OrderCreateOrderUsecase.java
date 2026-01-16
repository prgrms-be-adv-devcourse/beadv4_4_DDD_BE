package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.Order;
import com.modeunsa.boundedcontext.order.domain.OrderItem;
import com.modeunsa.boundedcontext.order.domain.OrderMapper;
import com.modeunsa.boundedcontext.order.domain.OrderMember;
import com.modeunsa.boundedcontext.order.domain.OrderProduct;
import com.modeunsa.boundedcontext.order.out.OrderMemberRepository;
import com.modeunsa.boundedcontext.order.out.OrderProductRepository;
import com.modeunsa.boundedcontext.order.out.OrderRepository;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.order.dto.CreateOrderRequestDto;
import com.modeunsa.shared.order.dto.OrderResponseDto;
import com.modeunsa.shared.order.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCreateOrderUsecase {
  private final OrderMemberRepository orderMemberRepository;
  private final OrderRepository orderRepository;
  private final OrderProductRepository orderProductRepository;
  private final OrderMapper orderMapper;
  private final SpringDomainEventPublisher eventPublisher;

  public OrderResponseDto createOrder(long memberId, CreateOrderRequestDto requestDto) {
    // 회원 확인
    OrderMember member =
        orderMemberRepository
            .findById(memberId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.ORDER_MEMBER_NOT_FOUND));

    // TODO: 실시간 조회로 수정
    OrderProduct product =
        orderProductRepository
            .findById(requestDto.getProductId())
            .orElseThrow(() -> new GeneralException(ErrorStatus.ORDER_PRODUCT_NOT_FOUND));

    // 재고 확인
    if (!product.isStockAvailable(requestDto.getQuantity())) {
      throw new GeneralException(ErrorStatus.ORDER_STOCK_NOT_ENOUGH);
    }

    // 주문 상품 생성
    OrderItem orderItem = orderMapper.toOrderItemEntity(product, requestDto);

    // 주문 생성 (단건 주문이라서 상품의 가격이 총금액) TODO: 주문 할인 도입
    Order order = orderMapper.toOrderEntity(member, orderItem.getSalePrice(), requestDto);

    order.addOrderItem(orderItem);

    // 주문을 저장하면 주문상품도 같이 저장
    orderRepository.save(order);

    // 재고 차감
    product.decreaseStock(requestDto.getQuantity());

    eventPublisher.publish(new OrderCreatedEvent(orderMapper.toOrderDto(order)));

    return orderMapper.toOrderResponseDto(order);
  }
}
