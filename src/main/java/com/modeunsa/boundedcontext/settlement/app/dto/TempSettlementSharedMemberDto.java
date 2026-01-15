package com.modeunsa.boundedcontext.settlement.app.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TempSettlementSharedMemberDto {
  private final Long memberId;
  private final String memberRole;
}
