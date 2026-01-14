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

  @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
  private String kakaoClientId;

  @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
  private String kakaoRedirectUri;

  @Override
  public OAuthProvider getProvider() {
    return OAuthProvider.KAKAO;
  }

  @Override
  public String generateOAuthUrl(String redirectUri) {
    String finalRedirectUri = redirectUri != null ? redirectUri : kakaoRedirectUri;
    String state = UUID.randomUUID().toString();

    // Redis에 state 저장 (5분 TTL)
    redisTemplate.opsForValue().set(
        "oauth:state:" + state,
        "KAKAO",  // provider 정보도 같이 저장
        Duration.ofMinutes(5)
    );

    return UriComponentsBuilder.fromUriString("https://kauth.kakao.com/oauth/authorize")
        .queryParam("client_id", kakaoClientId)
        .queryParam("redirect_uri", finalRedirectUri)
        .queryParam("response_type", "code")
        .queryParam("state", state)
        .build()
        .toUriString();
  }
}
