package com.modeunsa.boundedcontext.payment.app.mapper;

import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberDto;
import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberResponse;
import com.modeunsa.boundedcontext.payment.app.dto.order.PaymentOrderInfo;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.domain.types.MemberStatus;
import com.modeunsa.shared.auth.event.MemberSignupEvent;
import com.modeunsa.shared.order.dto.OrderDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

  PaymentOrderInfo toPaymentOrderInfo(OrderDto orderDto);

  @Mapping(target = "customerKey", source = "paymentMember.customerKey")
  @Mapping(target = "customerName", source = "paymentMember.name")
  @Mapping(target = "customerEmail", source = "paymentMember.email")
  @Mapping(target = "balance", source = "paymentAccount.balance")
  PaymentMemberResponse toPaymentMemberResponse(
      PaymentMember paymentMember, PaymentAccount paymentAccount);

  @Mapping(target = "id", source = "memberId")
  @Mapping(target = "name", source = "realName")
  @Mapping(target = "status", source = "status", qualifiedByName = "mapMemberStatus")
  PaymentMemberDto toPaymentMemberDto(MemberSignupEvent memberSignupEvent);

  @Named("mapMemberStatus")
  default MemberStatus mapMemberStatus(
      com.modeunsa.boundedcontext.member.domain.types.MemberStatus status) {
    if (status == null) {
      return MemberStatus.ACTIVE;
    }
    return switch (status) {
      case ACTIVE -> MemberStatus.ACTIVE;
      case SUSPENDED -> MemberStatus.INACTIVE;
      case WITHDRAWN_PENDING -> MemberStatus.INACTIVE;
      case WITHDRAWN -> MemberStatus.WITHDRAWN;
    };
  }
}
