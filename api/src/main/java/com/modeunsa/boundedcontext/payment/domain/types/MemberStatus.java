package com.modeunsa.boundedcontext.payment.domain.types;

import lombok.Getter;

@Getter
public enum MemberStatus {
  ACTIVE("활성 상태"),
  INACTIVE("비활성 상태"),
  WITHDRAWN("탈퇴 상태");

  private final String description;

  MemberStatus(String description) {
    this.description = description;
  }
}
