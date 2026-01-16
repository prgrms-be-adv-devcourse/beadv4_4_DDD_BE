package com.modeunsa.boundedcontext.payment.app.mapper;

import com.modeunsa.shared.order.dto.OrderDto;
import com.modeunsa.shared.payment.dto.PaymentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
  @Mapping(target = "buyerId", source = "memberId")
  PaymentDto toPaymentDto(OrderDto orderDto);
}
