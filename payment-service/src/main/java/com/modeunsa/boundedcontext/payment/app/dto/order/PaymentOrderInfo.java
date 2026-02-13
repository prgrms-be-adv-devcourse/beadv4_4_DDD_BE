package com.modeunsa.boundedcontext.payment.app.dto.order;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record PaymentOrderInfo(
    Long orderId,
    Long memberId,
    String orderNo,
    String status,
    BigDecimal totalAmount,
    String paymentDeadlineAt) {}
