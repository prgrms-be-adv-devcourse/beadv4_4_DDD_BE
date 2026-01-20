package com.modeunsa.shared.member.event;

public record MemberDeliveryAddressSetAsDefaultEvent(
    Long memberId,
    Long newDefaultDeliveryAddressId
) {
  public static MemberDeliveryAddressSetAsDefaultEvent of(Long memberId, Long newDefaultDeliveryAddressId) {
    return new MemberDeliveryAddressSetAsDefaultEvent(memberId, newDefaultDeliveryAddressId);
  }
}