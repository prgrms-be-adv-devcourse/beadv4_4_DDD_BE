package com.modeunsa.shared.member.event;

import com.modeunsa.boundedcontext.member.domain.entity.MemberSeller;

public record SellerRegisteredEvent(MemberSeller memberSeller) {}
