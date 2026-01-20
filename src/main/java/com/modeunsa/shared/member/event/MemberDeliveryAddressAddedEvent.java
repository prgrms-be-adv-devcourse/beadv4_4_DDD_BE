package com.modeunsa.shared.member.event;

public record MemberDeliveryAddressAddedEvent(
    Long memberId, Long addressId, String addressName, boolean isDefault) {
  public static MemberDeliveryAddressAddedEvent of(
      Long memberId, Long addressId, String addressName, boolean isDefault) {
    return new MemberDeliveryAddressAddedEvent(memberId, addressId, addressName, isDefault);
  }
}
