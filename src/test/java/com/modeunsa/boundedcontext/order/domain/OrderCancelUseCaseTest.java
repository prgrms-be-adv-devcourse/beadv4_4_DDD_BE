package com.modeunsa.boundedcontext.order.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.modeunsa.boundedcontext.order.app.OrderCancelOrderUseCase;
import com.modeunsa.boundedcontext.order.out.OrderRepository;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.order.event.OrderCancelRequestEvent;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class OrderCancelUseCaseTest {

  @InjectMocks private OrderCancelOrderUseCase orderCancelOrderUseCase;

  @Mock private OrderRepository orderRepository; // 가짜 리포지토리

  @Mock private SpringDomainEventPublisher eventPublisher; // 가짜 이벤트 발행기

  @Spy private OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);

  @Test
  @DisplayName("주문 취소 시 이벤트가 정상적으로 발행되어야 한다")
  void cancelOrder_success() {
    Long memberId = 1L;
    Long orderId = 100L;

    OrderMember mockMember = OrderMember.builder().build();
    ReflectionTestUtils.setField(mockMember, "id", memberId);

    Order mockOrder = Order.builder().orderMember(mockMember).status(OrderStatus.PAID).build();
    ReflectionTestUtils.setField(mockOrder, "id", orderId);

    given(orderRepository.findByIdWithFetch(orderId)).willReturn(Optional.of(mockOrder));

    // When
    orderCancelOrderUseCase.cancelOrder(1L, orderId);

    // Then
    verify(eventPublisher, times(1)).publish(any(OrderCancelRequestEvent.class));
    // 상태가 바뀌었는지 확인
    assertThat(mockOrder.getStatus()).isEqualTo(OrderStatus.CANCEL_REQUESTED);
  }

  @Test
  @DisplayName("이미 배송 중인 주문은 취소할 수 없다")
  void cancelOrder_fail_invalidStatus() {
    // Given
    Long memberId = 1L;
    Long orderId = 100L;

    OrderMember mockMember = OrderMember.builder().build();
    ReflectionTestUtils.setField(mockMember, "id", memberId);

    // 상태 SHIPPING
    Order mockOrder =
        Order.builder()
            .orderMember(mockMember)
            .status(OrderStatus.SHIPPING) // 취소 불가능한 상태
            .build();
    ReflectionTestUtils.setField(mockOrder, "id", orderId);

    given(orderRepository.findByIdWithFetch(orderId)).willReturn(Optional.of(mockOrder));

    // When & Then
    assertThatThrownBy(() -> orderCancelOrderUseCase.cancelOrder(memberId, orderId))
        .isInstanceOf(GeneralException.class)
        .hasFieldOrPropertyWithValue("errorStatus", ErrorStatus.ORDER_CANNOT_CANCEL); // 에러 메시지 확인
  }

  @Test
  @DisplayName("주문자가 아닌 다른 사람은 취소할 수 없다")
  void cancelOrder_fail_accessDenied() {
    // Given
    Long ownerId = 1L;
    Long orderId = 100L;

    // 주인 ID로 멤버 생성
    OrderMember owner = OrderMember.builder().build();
    ReflectionTestUtils.setField(owner, "id", ownerId);

    // 주인 소유의 주문 생성
    Order mockOrder = Order.builder().orderMember(owner).status(OrderStatus.PAID).build();
    ReflectionTestUtils.setField(mockOrder, "id", orderId);

    given(orderRepository.findByIdWithFetch(orderId)).willReturn(Optional.of(mockOrder));

    // When & Then
    Long strangerId = 999L;
    assertThatThrownBy(() -> orderCancelOrderUseCase.cancelOrder(strangerId, orderId))
        .isInstanceOf(GeneralException.class)
        .hasFieldOrPropertyWithValue("errorStatus", ErrorStatus.ORDER_ACCESS_DENIED);
  }
}
