package com.modeunsa.shared.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthStatusResponse {
  private boolean isAuthenticated;
  private String memberId;
}