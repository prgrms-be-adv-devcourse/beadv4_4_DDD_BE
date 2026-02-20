package com.modeunsa.boundedcontext.auth.out.client;

import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.boundedcontext.auth.domain.dto.OAuthProviderTokenResponse;
import com.modeunsa.boundedcontext.auth.domain.dto.OAuthUserInfo;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverOAuthClient implements OAuthClient {
  private final StringRedisTemplate redisTemplate;
  private final OAuthClientProperties properties;
  private final RestClient restClient = RestClient.create();

  private static final String TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
  private static final String USER_INFO_URL = "https://openapi.naver.com/v1/nid/me";

  @Override
  public OAuthProvider getProvider() {
    return OAuthProvider.NAVER;
  }

  @Override
  public String generateOAuthUrl(String redirectUri) {
    OAuthClientProperties.Registration naverProps = properties.registration().get("naver");

    String finalRedirectUri = redirectUri != null ? redirectUri : naverProps.redirectUri();
    String state = UUID.randomUUID().toString();

    // Redis에 state 저장 (5분 TTL)
    redisTemplate.opsForValue().set("oauth:state:" + state, "NAVER", Duration.ofMinutes(5));

    return UriComponentsBuilder.fromUriString("https://nid.naver.com/oauth2.0/authorize")
        .queryParam("client_id", naverProps.clientId())
        .queryParam("redirect_uri", finalRedirectUri)
        .queryParam("response_type", "code")
        .queryParam("state", state)
        .build()
        .toUriString();
  }

  @SuppressWarnings("unchecked")
  @Override
  public OAuthProviderTokenResponse getToken(String code, String redirectUri) {
    OAuthClientProperties.Registration naverProps = properties.registration().get("naver");
    String finalRedirectUri = redirectUri != null ? redirectUri : naverProps.redirectUri();

    String uri =
        UriComponentsBuilder.fromUriString(TOKEN_URL)
            .queryParam("grant_type", "authorization_code")
            .queryParam("client_id", naverProps.clientId())
            .queryParam("client_secret", naverProps.clientSecret())
            .queryParam("redirect_uri", finalRedirectUri)
            .queryParam("code", code)
            .build()
            .toUriString();

    try {
      Map<String, Object> response =
          restClient.get().uri(uri).retrieve().body(new ParameterizedTypeReference<>() {});

      return OAuthProviderTokenResponse.of(
          (String) response.get("access_token"),
          (String) response.get("refresh_token"),
          (String) response.get("token_type"),
          Long.parseLong((String) response.get("expires_in")));
    } catch (Exception e) {
      log.error("네이버 토큰 요청 실패: {}", e.getMessage());
      throw new GeneralException(ErrorStatus.OAUTH_TOKEN_REQUEST_FAILED);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public OAuthUserInfo getUserInfo(String accessToken) {
    try {
      Map<String, Object> response =
          restClient
              .get()
              .uri(USER_INFO_URL)
              .header("Authorization", "Bearer " + accessToken)
              .retrieve()
              .body(new ParameterizedTypeReference<>() {});

      Map<String, Object> naverResponse = (Map<String, Object>) response.get("response");

      String id = (String) naverResponse.get("id");
      String email = (String) naverResponse.get("email");
      String name = (String) naverResponse.get("name");
      String mobile = (String) naverResponse.get("mobile");

      return OAuthUserInfo.of(OAuthProvider.NAVER, id, email, name, mobile);
    } catch (Exception e) {
      log.error("네이버 사용자 정보 요청 실패: {}", e.getMessage());
      throw new GeneralException(ErrorStatus.OAUTH_USER_INFO_FAILED);
    }
  }
}
