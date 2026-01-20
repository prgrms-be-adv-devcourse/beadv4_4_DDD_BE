package com.modeunsa.shared.member.dto.response;

import com.modeunsa.boundedcontext.member.domain.entity.MemberDeliveryAddress;
import lombok.Builder;

@Builder
public record MemberDeliveryAddressResponse(
    Long id,
    String recipientName,
    String addressName,
    String address,
    String addressDetail,
    Boolean isDefault
) {
  public static MemberDeliveryAddressResponse from(MemberDeliveryAddress address) {
    return MemberDeliveryAddressResponse.builder()
        .id(address.getId())
        .recipientName(address.getRecipientName())
        .addressName(address.getAddressName())
        .address(address.getAddress())
        .addressDetail(address.getAddressDetail())
        .isDefault(address.getIsDefault())
        .build();
  }
}