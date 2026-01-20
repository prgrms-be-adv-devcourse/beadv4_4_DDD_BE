package com.modeunsa.shared.member.event;

public record MemberDeliveryAddressDeletedEvent(Long memberId, Long deletedDeliveryAddressId) {
  public static MemberDeliveryAddressDeletedEvent of(Long memberId, Long deletedDeliveryAddressId) {
    return new MemberDeliveryAddressDeletedEvent(memberId, deletedDeliveryAddressId);
  }
}
