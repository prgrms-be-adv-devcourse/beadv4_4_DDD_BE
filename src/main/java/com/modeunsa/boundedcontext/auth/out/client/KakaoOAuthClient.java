package com.modeunsa.boundedcontext.auth.out.client;

import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class KakaoOAuthClient implements OAuthClient {
  private final StringRedisTemplate redisTemplate;
  private final OAuthClientProperties properties;

  @Override
  public OAuthProvider getProvider() {
    return OAuthProvider.KAKAO;
  }

  @Override
  public String generateOAuthUrl(String redirectUri) {
    OAuthClientProperties.Registration kakaoProps = properties.getRegistration().get("kakao");

    String finalRedirectUri = redirectUri != null ? redirectUri : kakaoProps.getRedirectUri();
    String state = UUID.randomUUID().toString();

    // Redis에 state 저장 (5분 TTL)
    redisTemplate.opsForValue().set(
        "oauth:state:" + state,
        "KAKAO",
        Duration.ofMinutes(5)
    );

    return UriComponentsBuilder.fromUriString("https://kauth.kakao.com/oauth/authorize")
        .queryParam("client_id", kakaoProps.getClientId())
        .queryParam("redirect_uri", finalRedirectUri)
        .queryParam("response_type", "code")
        .queryParam("state", state)
        .build()
        .toUriString();
  }
}
