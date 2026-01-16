package com.modeunsa.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.auth.out.client.NaverOAuthClient;
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
class NaverOAuthClientTest {

  @Mock private StringRedisTemplate redisTemplate;

  @Mock private ValueOperations<String, String> valueOperations;

  @Mock private OAuthClientProperties properties;

  private NaverOAuthClient naverOAuthClient;

  @BeforeEach
  void setUp() {
    // 1. 테스트용 프로퍼티 설정
    OAuthClientProperties.Registration registration = new OAuthClientProperties.Registration();
    registration.setClientId("test-naver-client-id");
    registration.setRedirectUri("http://127.0.0.1:8080/login/oauth2/code/naver");

    // 2. Mock 동작 정의 (Registration Map 반환)
    lenient().when(properties.getRegistration()).thenReturn(Map.of("naver", registration));

    // 3. 생성자 주입
    naverOAuthClient = new NaverOAuthClient(redisTemplate, properties);
  }

  @Test
  @DisplayName("네이버 OAuth Provider 반환")
  void getProvider_returnsNaver() {
    assertThat(naverOAuthClient.getProvider()).isEqualTo(OAuthProvider.NAVER);
  }

  @Test
  @DisplayName("기본 redirect URI로 OAuth URL 생성")
  void generateOAuthUrl_withDefaultRedirectUri() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    String url = naverOAuthClient.generateOAuthUrl(null);
    String provider = OAuthProvider.NAVER.name();

    // URL 검증
    assertThat(url).contains("https://nid.naver.com/oauth2.0/authorize");
    assertThat(url).contains("client_id=test-naver-client-id");
    assertThat(url).contains("redirect_uri=http://127.0.0.1:8080/login/oauth2/code/naver");
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
    String url = naverOAuthClient.generateOAuthUrl(customRedirectUri);

    assertThat(url).contains("redirect_uri=http://localhost:3000/callback");
  }

  @Test
  @DisplayName("OAuth URL에 state 파라미터 포함 확인")
  void generateOAuthUrl_containsStateParameter() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    String url = naverOAuthClient.generateOAuthUrl(null);

    assertThat(url).containsPattern("state=[a-f0-9\\-]{36}");
  }
}
