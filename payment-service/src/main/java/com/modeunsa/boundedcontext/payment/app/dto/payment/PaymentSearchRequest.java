package com.modeunsa.boundedcontext.payment.app.dto.payment;

import com.modeunsa.boundedcontext.payment.app.dto.PageableRequest;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentStatus;
import com.modeunsa.boundedcontext.payment.domain.types.ProviderType;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public record PaymentSearchRequest(
    @PositiveOrZero int page,
    @PositiveOrZero int size,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
    PaymentStatus status,
    String orderNo,
    ProviderType paymentProvider)
    implements PageableRequest {}
