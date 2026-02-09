package com.modeunsa.shared.member.dto.response;

import com.modeunsa.boundedcontext.member.domain.entity.MemberDeliveryAddress;
import lombok.Builder;

@Builder
public record MemberDeliveryAddressResponse(
    Long id,
    String recipientName,
    String recipientPhone,
    String zipCode,
    String address,
    String addressDetail,
    String addressName,
    Boolean isDefault) {
  public static MemberDeliveryAddressResponse from(MemberDeliveryAddress address) {
    return MemberDeliveryAddressResponse.builder()
        .id(address.getId())
        .recipientName(address.getRecipientName())
        .recipientPhone(address.getRecipientPhone())
        .zipCode(address.getZipCode())
        .address(address.getAddress())
        .addressDetail(address.getAddressDetail())
        .addressName(address.getAddressName())
        .isDefault(address.getIsDefault())
        .build();
  }
}
