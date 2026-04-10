package com.modeunsa.boundedcontext.settlement.domain.types;

import lombok.Getter;

@Getter
public enum SettlementStatus {
  PENDING("정산 대기"),
  PROCESSING("정산 진행중"),
  COMPLETED("정산 완료");

  private final String description;

  SettlementStatus(String description) {
    this.description = description;
  }
}
