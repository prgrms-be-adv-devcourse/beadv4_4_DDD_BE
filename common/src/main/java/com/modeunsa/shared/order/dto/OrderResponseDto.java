package com.modeunsa.shared.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class OrderResponseDto {
  private long memberId;
  private long orderId;
  private String orderNo;
  private BigDecimal totalAmount;
  private String status;

  private List<OrderItemResponseDto> orderItems;

  private String recipientName;
  private String recipientPhone;
  private String zipCode;
  private String address;
  private String addressDetail;

  private LocalDateTime createdAt;
}
