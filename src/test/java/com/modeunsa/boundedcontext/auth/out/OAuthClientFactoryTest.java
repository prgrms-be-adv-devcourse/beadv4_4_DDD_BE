package com.modeunsa.boundedcontext.auth.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.auth.out.client.OAuthClient;
import com.modeunsa.boundedcontext.auth.out.client.OAuthClientFactory;
import com.modeunsa.global.exception.GeneralException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OAuthClientFactoryTest {

  @Test
  @DisplayName("등록된 Provider로 클라이언트 조회 성공")
  void getClient_withValidProvider_returnsClient() {
    // given
    OAuthClient kakaoClient = mock(OAuthClient.class);
    when(kakaoClient.getProvider()).thenReturn(OAuthProvider.KAKAO);

    OAuthClientFactory factory = new OAuthClientFactory(List.of(kakaoClient));

    // when
    OAuthClient result = factory.getClient(OAuthProvider.KAKAO);

    // then
    assertThat(result).isEqualTo(kakaoClient);
  }

  @Test
  @DisplayName("등록되지 않은 Provider로 조회 시 예외 발생")
  void getClient_withInvalidProvider_throwsException() {
    // given
    OAuthClient kakaoClient = mock(OAuthClient.class);
    when(kakaoClient.getProvider()).thenReturn(OAuthProvider.KAKAO);

    OAuthClientFactory factory = new OAuthClientFactory(List.of(kakaoClient));

    // when & then
    assertThatThrownBy(() -> factory.getClient(OAuthProvider.NAVER))
        .isInstanceOf(GeneralException.class);
  }

  @Test
  @DisplayName("여러 Provider 등록 후 각각 조회 성공")
  void getClient_withMultipleProviders_returnsCorrectClient() {
    // given
    OAuthClient kakaoClient = mock(OAuthClient.class);
    OAuthClient naverClient = mock(OAuthClient.class);
    when(kakaoClient.getProvider()).thenReturn(OAuthProvider.KAKAO);
    when(naverClient.getProvider()).thenReturn(OAuthProvider.NAVER);

    OAuthClientFactory factory = new OAuthClientFactory(List.of(kakaoClient, naverClient));

    // when
    OAuthClient kakaoResult = factory.getClient(OAuthProvider.KAKAO);
    OAuthClient naverResult = factory.getClient(OAuthProvider.NAVER);

    // then
    assertThat(kakaoResult).isEqualTo(kakaoClient);
    assertThat(naverResult).isEqualTo(naverClient);
  }
}
