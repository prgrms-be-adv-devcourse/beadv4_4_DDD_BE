package com.modeunsa.boundedcontext.payment.app.dto;

import com.modeunsa.boundedcontext.payment.domain.types.MemberStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author : JAKE
 * @date : 26. 1. 13.
 */
@Getter
@RequiredArgsConstructor
public class PaymentMemberDto {

  private final Long memberId;

  private final String email;

  private final String name;

  private final String customerKey;

  private final MemberStatus status;
}
