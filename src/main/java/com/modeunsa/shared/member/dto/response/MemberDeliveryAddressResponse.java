package com.modeunsa.shared.member.dto.response;

import com.modeunsa.boundedcontext.member.domain.entity.MemberDeliveryAddress;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberDeliveryAddressResponse {
  private Long id;
  private String recipientName;
  private String addressName;
  private String address;
  private String addressDetail;
  private Boolean isDefault;

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
