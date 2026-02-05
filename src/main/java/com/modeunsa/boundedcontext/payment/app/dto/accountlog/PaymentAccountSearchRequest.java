package com.modeunsa.boundedcontext.payment.app.dto.accountlog;

import com.modeunsa.boundedcontext.payment.app.dto.PageableRequest;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public record PaymentAccountSearchRequest(
    @Positive int page,
    @Positive int size,
    @NotNull LocalDateTime from,
    @NotNull LocalDateTime to,
    PaymentEventType type)
    implements PageableRequest {}
