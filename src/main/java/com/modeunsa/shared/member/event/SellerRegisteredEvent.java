package com.modeunsa.shared.member.event;

import com.modeunsa.boundedcontext.member.domain.types.SellerStatus;

public record SellerRegisteredEvent(
    Long memberSellerId,
    String businessName,
    String representativeName,
    String settlementBankName,
    String settlementBankAccount,
    SellerStatus status) {}
