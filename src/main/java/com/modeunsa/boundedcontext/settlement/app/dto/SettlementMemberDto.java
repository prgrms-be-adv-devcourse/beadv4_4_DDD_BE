package com.modeunsa.boundedcontext.settlement.app.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SettlementMemberDto {
  private final Long memberId;
  private final String name;
}
