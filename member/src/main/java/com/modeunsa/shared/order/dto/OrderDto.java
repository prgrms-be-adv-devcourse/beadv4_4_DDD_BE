package com.modeunsa.shared.order.dto;

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
  private String status;
  private BigDecimal totalAmount;
  private LocalDateTime paymentDeadlineAt;
  private LocalDateTime paidAt;

  private List<OrderItemDto> orderItems;
}
