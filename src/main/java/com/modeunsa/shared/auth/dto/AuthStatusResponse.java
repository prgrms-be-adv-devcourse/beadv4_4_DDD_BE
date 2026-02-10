package com.modeunsa.shared.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthStatusResponse {
  @JsonProperty("isAuthenticated")
  private boolean isAuthenticated;
  private String memberId;
}