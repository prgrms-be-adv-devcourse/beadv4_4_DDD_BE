package com.modeunsa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthStatusResponse {
  @JsonProperty("isAuthenticated")
  private boolean isAuthenticated;

  private String memberId;
  private String role;
  private Long sellerId;
}
