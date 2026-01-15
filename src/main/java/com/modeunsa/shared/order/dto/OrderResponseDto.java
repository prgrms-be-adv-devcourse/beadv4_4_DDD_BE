package com.modeunsa.shared.order.dto;

import java.math.BigDecimal;
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

  private List<OrderItemResponseDto> orderItems;

  private String receiverName;
  private String receiverPhone;
  private String zipcode;
  private String addressDetail;
}
