package com.modeunsa.shared.member.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberDeliveryAddressCreateRequest {
  private String recipientName;
  private String recipientPhone;
  private String zipCode;
  private String address;
  private String addressDetail;
  private String addressName;
  private Boolean isDefault;
}
