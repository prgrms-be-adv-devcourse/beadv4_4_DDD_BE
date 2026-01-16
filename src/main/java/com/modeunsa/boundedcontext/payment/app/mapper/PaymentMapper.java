package com.modeunsa.boundedcontext.payment.app.mapper;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentRequest;
import com.modeunsa.boundedcontext.payment.app.event.PaymentRequestEvent;
import com.modeunsa.shared.order.dto.OrderDto;
import com.modeunsa.shared.payment.dto.PaymentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
  PaymentRequest toPaymentRequestDto(PaymentRequestEvent event);

  @Mapping(target = "buyerId", source = "memberId")
  @Mapping(target = "pgPaymentAmount", source = "totalAmount")
  PaymentDto toPaymentDto(OrderDto orderDto);
}
