package com.modeunsa.boundedcontext.payment.app.dto.payment;

import com.modeunsa.boundedcontext.payment.domain.types.PaymentStatus;
import com.modeunsa.boundedcontext.payment.domain.types.ProviderType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentListItemResponse(
    String orderNo,
    Long orderId,
    PaymentStatus status,
    BigDecimal totalAmount,
    String productName,
    ProviderType paymentProvider,
    LocalDateTime createdAt) {}
