package com.modeunsa.shared.member.event;

public record MemberDeliveryAddressUpdatedEvent(
    Long memberId,
    Long deliveryAddressId
) {
  public static MemberDeliveryAddressUpdatedEvent of(Long memberId, Long deliveryAddressId) {
    return new MemberDeliveryAddressUpdatedEvent(memberId, deliveryAddressId);
  }
}