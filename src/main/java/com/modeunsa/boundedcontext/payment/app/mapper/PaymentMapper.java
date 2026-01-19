package com.modeunsa.boundedcontext.payment.app.mapper;

import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberResponse;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.shared.order.dto.OrderDto;
import com.modeunsa.shared.payment.dto.PaymentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
  @Mapping(target = "buyerId", source = "memberId")
  PaymentDto toPaymentDto(OrderDto orderDto);

  @Mapping(target = "customerKey", source = "paymentMember.customerKey")
  @Mapping(target = "customerName", source = "paymentMember.name")
  @Mapping(target = "customerEmail", source = "paymentMember.email")
  @Mapping(target = "balance", source = "paymentAccount.balance")
  PaymentMemberResponse toPaymentMemberResponse(
      PaymentMember paymentMember, PaymentAccount paymentAccount);
}
