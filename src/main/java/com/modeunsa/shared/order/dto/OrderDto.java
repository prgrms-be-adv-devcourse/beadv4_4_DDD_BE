package com.modeunsa.shared.order.dto;

import com.modeunsa.boundedcontext.order.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderDto {
  private Long orderId;
  private Long memberId;
  private String orderNo;
  private OrderStatus status;
  private BigDecimal totalAmount;
  private LocalDateTime paymentDeadlineAt;

  private List<OrderItemDto> orderItems;
}
