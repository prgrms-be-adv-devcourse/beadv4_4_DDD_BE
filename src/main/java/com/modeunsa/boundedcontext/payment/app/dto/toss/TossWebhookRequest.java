package com.modeunsa.boundedcontext.payment.app.dto.toss;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record TossWebhookRequest(
    @NotBlank String eventType, @NotNull LocalDateTime createdAt, @NotNull TossWebhookData data) {}
