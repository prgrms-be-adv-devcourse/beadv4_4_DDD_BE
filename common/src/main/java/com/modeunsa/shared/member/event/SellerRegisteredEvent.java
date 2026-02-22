package com.modeunsa.shared.member.event;

public record SellerRegisteredEvent(
    Long memberId,
    Long memberSellerId,
    String businessName,
    String representativeName,
    String settlementBankName,
    String settlementBankAccount,
    String status) {}
