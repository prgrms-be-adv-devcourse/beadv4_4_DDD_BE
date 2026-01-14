package com.modeunsa.boundedcontext.order.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.modeunsa.boundedcontext.order.app.OrderFacade;
import com.modeunsa.shared.order.dto.CreateOrderRequestDto;
import com.modeunsa.shared.order.event.OrderCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@RecordApplicationEvents
class OrderCreateUseCaseTest {

  @Autowired private OrderFacade orderFacade;

  @Autowired private ApplicationEvents events;

  @Test
  @Transactional
  @DisplayName("주문생성시 이벤트가 발행되어야 한다") // 여기에 한글 설명!
  void shouldPublishEventWhenOrderCreated() {
    // given
    Long memberId = 1L;
    // when
    orderFacade.createOrder(
        memberId,
        new CreateOrderRequestDto(
            2, // productId
            7, // quantity (2개 구매)
            "세종대왕", // receiverName
            "010-1234-5678", // receiverPhone
            "12345", // zipcode
            "서울시 강남구 테헤란로 123" // addressDetail
            ));

    // then
    // OrderCreatedEvent 타입의 이벤트가 정확히 1번 발행되었는지 확인
    long count = events.stream(OrderCreatedEvent.class).count();
    assertThat(count).isEqualTo(1);

    // 발행된 이벤트의 내용 확인
    OrderCreatedEvent event = events.stream(OrderCreatedEvent.class).findFirst().orElseThrow();

    assertThat(event.getOrderDto().getMemberId()).isEqualTo(memberId);
  }
}
