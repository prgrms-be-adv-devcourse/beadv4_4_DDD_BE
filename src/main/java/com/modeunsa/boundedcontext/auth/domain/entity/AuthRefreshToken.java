package com.modeunsa.boundedcontext.auth.domain.entity;

import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@RedisHash(value = "refresh")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRefreshToken {

  @Id private Long memberId;

  private String refreshToken;

  @TimeToLive(unit = TimeUnit.MILLISECONDS)
  private Long expiration;

  public boolean isTokenMatching(String inputToken) {
    return this.refreshToken.equals(inputToken);
  }
}
