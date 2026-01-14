package com.modeunsa.boundedcontext.payment.app.mapper;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentRequest;
import com.modeunsa.boundedcontext.payment.app.event.PaymentRequestEvent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
  PaymentRequest toPaymentRequestDto(PaymentRequestEvent event);
}
