package com.modeunsa.shared.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderListResponseDto {
  private Long orderId;
  private String orderNo; // 주문 번호
  private LocalDateTime orderedAt; // 주문 일시

  private String repProductName; // "신발 외 2건" 처럼 가공된 문자열
  private Integer totalCnt; // 총 수량
  private BigDecimal totalAmount; // 총 결제 금액
  private String status; // 주문 상태 (결제대기, 배송중 등)

  private LocalDateTime paymentDeadlineAt;
  private LocalDateTime createdAt;
}
