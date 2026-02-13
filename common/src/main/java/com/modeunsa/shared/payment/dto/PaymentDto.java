package com.modeunsa.shared.payment.dto;

import java.math.BigDecimal;

public record PaymentDto(Long orderId, String orderNo, Long memberId, BigDecimal totalAmount) {}
