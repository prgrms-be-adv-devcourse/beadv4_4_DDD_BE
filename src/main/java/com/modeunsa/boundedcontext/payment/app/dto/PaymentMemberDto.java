package com.modeunsa.boundedcontext.payment.app.dto;

import com.modeunsa.boundedcontext.payment.domain.types.MemberStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentMemberDto {

  private final Long id;

  private final String email;

  private final String name;

  private final MemberStatus status;
}
