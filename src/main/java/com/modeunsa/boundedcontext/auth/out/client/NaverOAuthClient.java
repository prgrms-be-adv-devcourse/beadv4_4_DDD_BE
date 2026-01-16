package com.modeunsa.boundedcontext.auth.out.client;

import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class NaverOAuthClient implements OAuthClient {
  private final StringRedisTemplate redisTemplate;
  private final OAuthClientProperties properties;

  @Override
  public OAuthProvider getProvider() {
    return OAuthProvider.NAVER;
  }

  @Override
  public String generateOAuthUrl(String redirectUri) {
    OAuthClientProperties.Registration naverProps = properties.getRegistration().get("naver");

    String finalRedirectUri = redirectUri != null ? redirectUri : naverProps.getRedirectUri();
    String state = UUID.randomUUID().toString();

    // Redis에 state 저장 (5분 TTL)
    // TODO: OAuth 콜백 시 state 검증 로직 추가 예정
    redisTemplate.opsForValue().set("oauth:state:" + state, "NAVER", Duration.ofMinutes(5));

    return UriComponentsBuilder.fromUriString("https://nid.naver.com/oauth2.0/authorize")
        .queryParam("client_id", naverProps.getClientId())
        .queryParam("redirect_uri", finalRedirectUri)
        .queryParam("response_type", "code")
        .queryParam("state", state)
        .build()
        .toUriString();
  }
}
