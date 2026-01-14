package com.modeunsa.shared.order.dto;

import com.modeunsa.boundedcontext.order.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderDto {
  private long memberId;
  private String orderNo;
  private OrderStatus status;
  private BigDecimal totalAmount;
  private LocalDateTime paymentDeadlineAt;
}
