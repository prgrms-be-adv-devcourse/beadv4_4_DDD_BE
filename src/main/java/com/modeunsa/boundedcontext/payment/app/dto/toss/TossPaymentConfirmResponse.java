package com.modeunsa.boundedcontext.payment.app.dto.toss;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record TossPaymentConfirmResponse(
    @JsonProperty("paymentKey") String paymentKey,
    @JsonProperty("orderId") String orderId,
    @JsonProperty("orderName") String orderName,
    @JsonProperty("method") String method,
    @JsonProperty("totalAmount") BigDecimal totalAmount,
    @JsonProperty("status") String status) {}
