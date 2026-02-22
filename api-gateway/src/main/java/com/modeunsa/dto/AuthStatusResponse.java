package com.modeunsa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthStatusResponse {
  @JsonProperty("isAuthenticated")
  private boolean isAuthenticated;

  private String memberId;
  private String role;
  private Long sellerId;
}
