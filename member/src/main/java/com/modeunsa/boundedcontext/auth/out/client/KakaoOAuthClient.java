package com.modeunsa.boundedcontext.auth.out.client;

import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.auth.dto.OAuthProviderTokenResponse;
import com.modeunsa.shared.auth.dto.OAuthUserInfo;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOAuthClient implements OAuthClient {
  private final StringRedisTemplate redisTemplate;
  private final OAuthClientProperties properties;
  private final RestClient restClient = RestClient.create();

  private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
  private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

  @Override
  public OAuthProvider getProvider() {
    return OAuthProvider.KAKAO;
  }

  @Override
  public String generateOAuthUrl(String redirectUri) {
    OAuthClientProperties.Registration kakaoProps = properties.registration().get("kakao");

    String finalRedirectUri = redirectUri != null ? redirectUri : kakaoProps.redirectUri();
    String state = UUID.randomUUID().toString();

    // Redis에 state 저장 (5분 TTL)
    redisTemplate.opsForValue().set("oauth:state:" + state, "KAKAO", Duration.ofMinutes(5));

    return UriComponentsBuilder.fromUriString("https://kauth.kakao.com/oauth/authorize")
        .queryParam("client_id", kakaoProps.clientId())
        .queryParam("redirect_uri", finalRedirectUri)
        .queryParam("response_type", "code")
        .queryParam("state", state)
        .build()
        .toUriString();
  }

  @Override
  public OAuthProviderTokenResponse getToken(String code, String redirectUri) {
    OAuthClientProperties.Registration kakaoProps = properties.registration().get("kakao");
    String finalRedirectUri = redirectUri != null ? redirectUri : kakaoProps.redirectUri();

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "authorization_code");
    params.add("client_id", kakaoProps.clientId());
    params.add("client_secret", kakaoProps.clientSecret());
    params.add("redirect_uri", finalRedirectUri);
    params.add("code", code);

    try {
      Map<String, Object> response =
          restClient
              .post()
              .uri(TOKEN_URL)
              .contentType(MediaType.APPLICATION_FORM_URLENCODED)
              .body(params)
              .retrieve()
              .body(new ParameterizedTypeReference<>() {});

      return OAuthProviderTokenResponse.of(
          (String) response.get("access_token"),
          (String) response.get("refresh_token"),
          (String) response.get("token_type"),
          ((Number) response.get("expires_in")).longValue());
    } catch (Exception e) {
      log.error("카카오 토큰 요청 실패: {}", e.getMessage());
      throw new GeneralException(ErrorStatus.OAUTH_TOKEN_REQUEST_FAILED);
    }
  }

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

      String id = String.valueOf(response.get("id"));

      return OAuthUserInfo.of(OAuthProvider.KAKAO, id, null, null, null);
    } catch (Exception e) {
      log.error("카카오 사용자 정보 요청 실패: {}", e.getMessage());
      throw new GeneralException(ErrorStatus.OAUTH_USER_INFO_FAILED);
    }
  }
}
