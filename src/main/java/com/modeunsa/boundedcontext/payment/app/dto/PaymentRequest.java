package com.modeunsa.boundedcontext.payment.app.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class PaymentRequest {
  @NotNull private Long orderId;
  @NotEmpty private String orderNo;
  @NotNull private Long buyerId;
  @Positive private BigDecimal totalAmount;
}
