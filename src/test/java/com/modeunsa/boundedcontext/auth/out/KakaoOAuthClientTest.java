package com.modeunsa.boundedcontext.auth.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.auth.out.client.KakaoOAuthClient;
import com.modeunsa.boundedcontext.auth.out.client.OAuthClientProperties;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class KakaoOAuthClientTest {

  @Mock private StringRedisTemplate redisTemplate;

  @Mock private ValueOperations<String, String> valueOperations;

  @Mock private OAuthClientProperties properties;

  private KakaoOAuthClient kakaoOAuthClient;

  @BeforeEach
  void setUp() {
    OAuthClientProperties.Registration registration =
        OAuthClientProperties.Registration.ofTest(
            "test-kakao-client-id", "http://127.0.0.1:8080/login/oauth2/code/kakao");

    lenient().when(properties.registration()).thenReturn(Map.of("kakao", registration));

    kakaoOAuthClient = new KakaoOAuthClient(redisTemplate, properties);
  }

  @Test
  @DisplayName("카카오 OAuth Provider 반환")
  void getProvider_returnsKakao() {
    assertThat(kakaoOAuthClient.getProvider()).isEqualTo(OAuthProvider.KAKAO);
  }

  @Test
  @DisplayName("기본 redirect URI로 OAuth URL 생성")
  void generateOAuthUrl_withDefaultRedirectUri() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    String url = kakaoOAuthClient.generateOAuthUrl(null);
    String provider = OAuthProvider.KAKAO.name();

    // URL 검증
    assertThat(url).contains("https://kauth.kakao.com/oauth/authorize");
    assertThat(url).contains("client_id=test-kakao-client-id");
    assertThat(url).contains("redirect_uri=http://127.0.0.1:8080/login/oauth2/code/kakao");
    assertThat(url).contains("response_type=code");
    assertThat(url).contains("state=");

    // Redis 저장 검증
    verify(valueOperations)
        .set(startsWith("oauth:state:"), eq(provider), eq(Duration.ofMinutes(5)));
  }

  @Test
  @DisplayName("커스텀 redirect URI로 OAuth URL 생성")
  void generateOAuthUrl_withCustomRedirectUri() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    String customRedirectUri = "http://localhost:3000/callback";
    String url = kakaoOAuthClient.generateOAuthUrl(customRedirectUri);

    assertThat(url).contains("redirect_uri=http://localhost:3000/callback");
    assertThat(url).doesNotContain("redirect_uri=http://127.0.0.1:8080");
  }

  @Test
  @DisplayName("OAuth URL에 state 파라미터 포함 확인")
  void generateOAuthUrl_containsStateParameter() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    String url = kakaoOAuthClient.generateOAuthUrl(null);

    assertThat(url).containsPattern("state=[a-f0-9\\-]{36}");
  }
}
