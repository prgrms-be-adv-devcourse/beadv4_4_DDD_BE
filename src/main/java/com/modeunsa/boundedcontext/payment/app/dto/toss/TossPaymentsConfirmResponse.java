package com.modeunsa.boundedcontext.payment.app.dto.toss;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TossPaymentsConfirmResponse(
    @JsonProperty("paymentKey") String paymentKey,
    @JsonProperty("orderId") String orderId,
    @JsonProperty("orderName") String orderName,
    @JsonProperty("method") String method,
    @JsonProperty("totalAmount") long totalAmount,
    @JsonProperty("status") String status) {}
