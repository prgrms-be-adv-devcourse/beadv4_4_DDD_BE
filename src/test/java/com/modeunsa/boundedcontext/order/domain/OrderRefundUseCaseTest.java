package com.modeunsa.boundedcontext.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.modeunsa.boundedcontext.order.app.OrderRefundOrderUseCase;
import com.modeunsa.boundedcontext.order.out.OrderRepository;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.order.dto.OrderDto;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OrderRefundUseCaseTest {

  @InjectMocks private OrderRefundOrderUseCase orderRefundOrderUseCase;

  @Mock private OrderRepository orderRepository;

  @Mock private OrderMapper orderMapper;

  @Mock private SpringDomainEventPublisher eventPublisher;

  @Test
  @DisplayName("배송 완료 후 7일 이내라면 반품 신청이 성공해야 한다")
  void requestRefund_success() {
    // Given
    Long memberId = 1L;
    Long orderId = 100L;

    OrderMember mockMember = OrderMember.builder().build();
    ReflectionTestUtils.setField(mockMember, "id", memberId);

    Order mockOrder =
        Order.builder()
            .orderMember(mockMember)
            .status(OrderStatus.DELIVERED) // 배송 완료 상태
            .deliveredAt(LocalDateTime.now().minusDays(3L))
            .build();
    ReflectionTestUtils.setField(mockOrder, "id", orderId);

    // ★ 시간 조작: 배송 완료된 지 3일 지남 (7일 이내 -> 성공 케이스)
    ReflectionTestUtils.setField(mockOrder, "deliveredAt", LocalDateTime.now().minusDays(3));

    // Support가 주문을 리턴하도록 설정
    given(orderRepository.findById(orderId)).willReturn(Optional.of(mockOrder));

    // ★ [추가된 부분] Mapper가 반환할 가짜 DTO 설정
    // (UseCase가 마지막에 mapper.toOrderDto를 호출하므로, 빈 껍데기 DTO라도 줘야 함)
    OrderDto fakeResponse = OrderDto.builder().orderId(orderId).build();

    // "어떤(any) Order 객체로 toOrderDto가 호출되면, fakeResponse를 리턴해라"
    given(orderMapper.toOrderDto(any(Order.class))).willReturn(fakeResponse);

    // When
    orderRefundOrderUseCase.refundOrder(memberId, orderId);

    // Then
    // 상태가 '환불 요청됨'으로 변했는지 확인
    assertThat(mockOrder.getStatus()).isEqualTo(OrderStatus.REFUND_REQUESTED);
  }

  @Test
  @DisplayName("배송 완료 상태가 아니면 반품 신청을 할 수 없다")
  void requestRefund_fail_invalidStatus() {
    // Given
    Long memberId = 1L;
    Long orderId = 100L;

    OrderMember mockMember = OrderMember.builder().build();
    ReflectionTestUtils.setField(mockMember, "id", memberId);

    // 실패 케이스: 아직 배송 중 (SHIPPING)
    Order mockOrder = Order.builder().orderMember(mockMember).status(OrderStatus.SHIPPING).build();
    ReflectionTestUtils.setField(
        mockOrder, "deliveredAt", LocalDateTime.now().minusDays(3)); // 날짜는 정상이지만 상태가 아님

    given(orderRepository.findById(orderId)).willReturn(Optional.of(mockOrder));

    // When & Then
    // Entity의 isRefundable()에서 false가 나와 UseCase가 예외를 던짐
    assertThatThrownBy(() -> orderRefundOrderUseCase.refundOrder(memberId, orderId))
        .isInstanceOf(GeneralException.class)
        .hasFieldOrPropertyWithValue("errorStatus", ErrorStatus.ORDER_CANNOT_REFUND);
  }

  @Test
  @DisplayName("배송 완료 후 7일이 지났으면 반품 신청을 할 수 없다")
  void requestRefund_fail_periodExpired() {
    // Given
    Long memberId = 1L;
    Long orderId = 100L;

    OrderMember mockMember = OrderMember.builder().build();
    ReflectionTestUtils.setField(mockMember, "id", memberId);

    Order mockOrder =
        Order.builder()
            .orderMember(mockMember)
            .status(OrderStatus.DELIVERED) // 상태는 정상이지만
            .deliveredAt(LocalDateTime.now().minusDays(8L))
            .build();

    // ★ 시간 조작: 배송 완료된 지 8일 지남 (기간 만료 -> 실패 케이스)
    ReflectionTestUtils.setField(mockOrder, "deliveredAt", LocalDateTime.now().minusDays(8));

    given(orderRepository.findById(orderId)).willReturn(Optional.of(mockOrder));

    // When & Then
    assertThatThrownBy(() -> orderRefundOrderUseCase.refundOrder(memberId, orderId))
        .isInstanceOf(GeneralException.class)
        .hasFieldOrPropertyWithValue("errorStatus", ErrorStatus.ORDER_CANNOT_REFUND);
  }

  @Test
  @DisplayName("주문자가 아닌 다른 사람은 반품 신청을 할 수 없다")
  void requestRefund_fail_accessDenied() {
    // Given
    Long ownerId = 1L;
    Long orderId = 100L;

    OrderMember owner = OrderMember.builder().build();
    ReflectionTestUtils.setField(owner, "id", ownerId);

    Order mockOrder = Order.builder().orderMember(owner).status(OrderStatus.DELIVERED).build();
    ReflectionTestUtils.setField(mockOrder, "deliveredAt", LocalDateTime.now());

    given(orderRepository.findById(orderId)).willReturn(Optional.of(mockOrder));

    // When & Then
    Long strangerId = 999L; // 다른 사람 ID
    assertThatThrownBy(() -> orderRefundOrderUseCase.refundOrder(strangerId, orderId))
        .isInstanceOf(GeneralException.class)
        .hasFieldOrPropertyWithValue("errorStatus", ErrorStatus.ORDER_ACCESS_DENIED); // 접근 권한 에러
  }
}
