package com.modeunsa.boundedcontext.payment.app.mapper;

import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberDto;
import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberResponse;
import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberSyncRequest;
import com.modeunsa.boundedcontext.payment.app.dto.order.PaymentOrderInfo;
import com.modeunsa.boundedcontext.payment.app.dto.settlement.PaymentPayoutInfo;
import com.modeunsa.boundedcontext.payment.domain.types.MemberStatus;
import com.modeunsa.shared.member.event.MemberSignupEvent;
import com.modeunsa.shared.order.dto.OrderDto;
import com.modeunsa.shared.settlement.dto.SettlementCompletedPayoutDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

  PaymentOrderInfo toPaymentOrderInfo(OrderDto orderDto);

  PaymentMemberResponse toPaymentMemberResponse(PaymentMemberDto paymentMemberDto);

  @Mapping(target = "id", source = "memberId")
  @Mapping(target = "name", source = "realName")
  @Mapping(target = "status", source = "status", qualifiedByName = "mapMemberStatus")
  PaymentMemberSyncRequest toPaymentMemberSyncRequest(MemberSignupEvent memberSignupEvent);

  @Named("mapMemberStatus")
  default MemberStatus mapMemberStatus(MemberStatus status) {
    if (status == null) {
      return MemberStatus.ACTIVE;
    }
    return switch (status) {
      case INACTIVE -> null;
      case PRE_ACTIVE, SUSPENDED, WITHDRAWN_PENDING -> MemberStatus.INACTIVE;
      case ACTIVE -> MemberStatus.ACTIVE;
      case WITHDRAWN -> MemberStatus.WITHDRAWN;
    };
  }

  List<PaymentPayoutInfo> toPaymentPayoutInfoList(List<SettlementCompletedPayoutDto> payouts);
}
