package com.modeunsa.boundedcontext.auth.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.modeunsa.boundedcontext.auth.app.usecase.OAuthAccountResolveUseCase;
import com.modeunsa.boundedcontext.auth.app.usecase.OAuthMemberRegisterUseCase;
import com.modeunsa.boundedcontext.auth.domain.entity.OAuthAccount;
import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.auth.out.repository.AuthSocialAccountRepository;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.shared.auth.dto.OAuthUserInfo;
import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class OAuthAccountResolveUseCaseTest {

  @InjectMocks private OAuthAccountResolveUseCase oauthAccountResolveUseCase;

  @Mock private AuthSocialAccountRepository socialAccountRepository;
  @Mock private OAuthMemberRegisterUseCase oauthMemberRegisterUseCase;

  private final OAuthProvider provider = OAuthProvider.KAKAO;
  private final String providerId = "kakao_12345";

  @Nested
  @DisplayName("소셜 계정 조회")
  class FindSocialAccount {

    @Test
    @DisplayName("기존 소셜 계정이 존재하면 조회하여 반환")
    void findExistingAccount() {
      // given
      OAuthUserInfo userInfo = createUserInfo();
      Member member = createMemberWithId(1L);
      OAuthAccount existingAccount = createSocialAccount(member);

      given(socialAccountRepository.findByOauthProviderAndProviderId(provider, providerId))
          .willReturn(Optional.of(existingAccount));

      // when
      OAuthAccount result = oauthAccountResolveUseCase.execute(provider, userInfo);

      // then
      assertThat(result).isEqualTo(existingAccount);
      verify(oauthMemberRegisterUseCase, never()).execute(any());
    }

    @Test
    @DisplayName("소셜 계정이 없으면 신규 가입 후 반환")
    void registerNewAccount() {
      // given
      OAuthUserInfo userInfo = createUserInfo();
      Member member = createMemberWithId(1L);
      OAuthAccount newAccount = createSocialAccount(member);

      given(socialAccountRepository.findByOauthProviderAndProviderId(provider, providerId))
          .willReturn(Optional.empty());
      given(oauthMemberRegisterUseCase.execute(userInfo)).willReturn(newAccount);

      // when
      OAuthAccount result = oauthAccountResolveUseCase.execute(provider, userInfo);

      // then
      assertThat(result).isEqualTo(newAccount);
      verify(oauthMemberRegisterUseCase, times(1)).execute(userInfo);
    }
  }

  @Nested
  @DisplayName("동시성 처리")
  class ConcurrencyHandling {

    @Test
    @DisplayName("동시 가입 요청으로 중복키 예외 발생 시 기존 계정 재조회하여 반환")
    void handleDuplicateKeyException() {
      // given
      OAuthUserInfo userInfo = createUserInfo();
      Member member = createMemberWithId(1L);
      OAuthAccount existingAccount = createSocialAccount(member);

      // 1차 조회: 없음
      // 신규 가입 시도: 중복키 예외
      // 2차 조회: 있음 (다른 요청이 먼저 생성)
      given(socialAccountRepository.findByOauthProviderAndProviderId(provider, providerId))
          .willReturn(Optional.empty())
          .willReturn(Optional.of(existingAccount));
      given(oauthMemberRegisterUseCase.execute(userInfo))
          .willThrow(new DataIntegrityViolationException("Duplicate key"));

      // when
      OAuthAccount result = oauthAccountResolveUseCase.execute(provider, userInfo);

      // then
      assertThat(result).isEqualTo(existingAccount);
      verify(socialAccountRepository, times(2))
          .findByOauthProviderAndProviderId(provider, providerId);
      verify(oauthMemberRegisterUseCase, times(1)).execute(userInfo);
    }

    @Test
    @DisplayName("중복키 예외 발생 후 재조회에도 실패하면 예외 발생")
    void handleDuplicateKeyExceptionWithRetryFailure() {
      // given
      OAuthUserInfo userInfo = createUserInfo();

      given(socialAccountRepository.findByOauthProviderAndProviderId(provider, providerId))
          .willReturn(Optional.empty())
          .willReturn(Optional.empty());
      given(oauthMemberRegisterUseCase.execute(userInfo))
          .willThrow(new DataIntegrityViolationException("Duplicate key"));

      // when & then
      assertThrows(
          GeneralException.class, () -> oauthAccountResolveUseCase.execute(provider, userInfo));

      verify(socialAccountRepository, times(2))
          .findByOauthProviderAndProviderId(provider, providerId);
    }
  }

  // --- Helper Methods ---

  private OAuthUserInfo createUserInfo() {
    return OAuthUserInfo.builder()
        .provider(provider)
        .providerId(providerId)
        .email("test@example.com")
        .name("테스트유저")
        .build();
  }

  private Member createMemberWithId(Long id) {
    Member member = Member.builder().email("test@example.com").realName("테스트유저").build();
    setEntityId(member, id);
    return member;
  }

  private OAuthAccount createSocialAccount(Member member) {
    OAuthAccount socialAccount =
        OAuthAccount.builder().oauthProvider(provider).providerId(providerId).build();
    socialAccount.assignMember(member);
    return socialAccount;
  }

  private void setEntityId(Object entity, Long id) {
    try {
      Field idField = findIdField(entity.getClass());
      idField.setAccessible(true);
      idField.set(entity, id);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set entity id", e);
    }
  }

  private Field findIdField(Class<?> clazz) throws NoSuchFieldException {
    Class<?> current = clazz;
    while (current != null) {
      try {
        return current.getDeclaredField("id");
      } catch (NoSuchFieldException e) {
        current = current.getSuperclass();
      }
    }
    throw new NoSuchFieldException("id field not found");
  }
}
