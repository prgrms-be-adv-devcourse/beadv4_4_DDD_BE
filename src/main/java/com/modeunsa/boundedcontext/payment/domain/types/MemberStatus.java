package com.modeunsa.boundedcontext.payment.domain.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author : JAKE
 * @date : 26. 1. 12.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum MemberStatus {
  ACTIVE("활성 상태"),
  INACTIVE("비활성 상태"),
  WITHDRAWN("탈퇴 상태");

  private String description;
}
