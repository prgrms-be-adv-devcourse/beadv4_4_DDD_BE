package com.modeunsa.boundedcontext.payment.app.dto.toss;

public record TossPaymentsConfirmRequest(String paymentKey, String orderId, long amount) {}
