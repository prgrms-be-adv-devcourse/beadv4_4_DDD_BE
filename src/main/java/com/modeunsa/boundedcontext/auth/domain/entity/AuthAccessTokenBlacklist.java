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
@RedisHash(value = "blacklist")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthAccessTokenBlacklist {

  @Id
  private String accessToken;

  private Long memberId;

  @TimeToLive(unit = TimeUnit.MILLISECONDS)
  private Long expiration;

  public static AuthAccessTokenBlacklist of(String accessToken, Long memberId, Long remainingExpiration) {
    return AuthAccessTokenBlacklist.builder()
        .accessToken(accessToken)
        .memberId(memberId)
        .expiration(remainingExpiration)
        .build();
  }
}
