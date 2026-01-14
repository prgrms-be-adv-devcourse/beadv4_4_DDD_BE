package com.modeunsa.auth;

import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.auth.out.client.KakaoOAuthClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KakaoOAuthClientTest {

  @Mock
  private StringRedisTemplate redisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  private KakaoOAuthClient kakaoOAuthClient;

  @BeforeEach
  void setUp() {
    kakaoOAuthClient = new KakaoOAuthClient(redisTemplate);
    ReflectionTestUtils.setField(kakaoOAuthClient, "kakaoClientId", "test-client-id");
    ReflectionTestUtils.setField(kakaoOAuthClient, "kakaoRedirectUri",
        "http://127.0.0.1:8080/login/oauth2/code/kakao");
  }

  @Test
  @DisplayName("카카오 OAuth Provider 반환")
  void getProvider_returnsKakao() {
    assertThat(kakaoOAuthClient.getProvider()).isEqualTo(OAuthProvider.KAKAO);
  }

  @Test
  @DisplayName("기본 redirect URI로 OAuth URL 생성")
  void generateOAuthUrl_withDefaultRedirectUri() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);  // 여기서 stubbing

    String url = kakaoOAuthClient.generateOAuthUrl(null);

    assertThat(url).contains("https://kauth.kakao.com/oauth/authorize");
    assertThat(url).contains("client_id=test-client-id");
    assertThat(url).contains("redirect_uri=http://127.0.0.1:8080/login/oauth2/code/kakao");
    assertThat(url).contains("response_type=code");
    assertThat(url).contains("state=");
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