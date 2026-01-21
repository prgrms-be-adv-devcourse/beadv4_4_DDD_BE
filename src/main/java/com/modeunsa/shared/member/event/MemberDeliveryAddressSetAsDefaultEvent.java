package com.modeunsa.shared.member.event;

public record MemberDeliveryAddressSetAsDefaultEvent(
    Long memberId,
    Long deliveryAddressId,
    String recipientName,
    String recipientPhone,
    String zipCode,
    String address,
    String addressDetail,
    String addressName,
    boolean isDefault) {}
