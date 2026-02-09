package com.modeunsa.boundedcontext.auth.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.modeunsa.boundedcontext.auth.app.usecase.DuplicateResolveUseCase;
import com.modeunsa.boundedcontext.auth.app.usecase.OAuthAccountResolveUseCase;
import com.modeunsa.boundedcontext.auth.app.usecase.OAuthMemberRegisterUseCase;
import com.modeunsa.boundedcontext.auth.domain.entity.OAuthAccount;
import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.auth.out.repository.AuthSocialAccountRepository;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.shared.auth.dto.OAuthUserInfo;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OAuthAccountResolveUseCaseTest {

  @Mock private AuthSocialAccountRepository socialAccountRepository;
  @Mock private OAuthMemberRegisterUseCase oauthMemberRegisterUseCase;
  @Mock private DuplicateResolveUseCase duplicateResolveUseCase;

  @InjectMocks private OAuthAccountResolveUseCase oauthAccountResolveUseCase;

  private final OAuthProvider provider = OAuthProvider.KAKAO;
  private final String providerId = "12345";

  @Test
  @DisplayName("기존 소셜 계정이 있으면 해당 계정을 반환한다")
  void returnExistingAccount() {
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
  }

  @Test
  @DisplayName("기존 소셜 계정이 없으면 신규 가입을 진행한다")
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
    verify(oauthMemberRegisterUseCase).execute(userInfo);
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

      given(socialAccountRepository.findByOauthProviderAndProviderId(provider, providerId))
          .willReturn(Optional.empty());

      given(oauthMemberRegisterUseCase.execute(any()))
          .willThrow(DataIntegrityViolationException.class);

      given(duplicateResolveUseCase.findExistingAccount(provider, providerId))
          .willReturn(existingAccount);

      // when
      OAuthAccount result = oauthAccountResolveUseCase.execute(provider, userInfo);

      // then
      assertThat(result).isEqualTo(existingAccount);
      verify(duplicateResolveUseCase).findExistingAccount(provider, providerId);
    }

    @Test
    @DisplayName("중복키 예외 발생 후 재조회에도 실패하면 예외 발생")
    void handleDuplicateKeyExceptionWithRetryFailure() {
      // given
      given(socialAccountRepository.findByOauthProviderAndProviderId(provider, providerId))
          .willReturn(Optional.empty());
      given(oauthMemberRegisterUseCase.execute(any()))
          .willThrow(DataIntegrityViolationException.class);

      given(duplicateResolveUseCase.findExistingAccount(provider, providerId))
          .willThrow(GeneralException.class);

      OAuthUserInfo userInfo = createUserInfo();

      // when & then
      assertThrows(
          GeneralException.class, () -> oauthAccountResolveUseCase.execute(provider, userInfo));
    }
  }

  private OAuthUserInfo createUserInfo() {
    return OAuthUserInfo.of(provider, providerId, "test@test.com", "테스터", "010-1234-5678");
  }

  private Member createMemberWithId(Long id) {
    Member member = Member.builder().build();
    ReflectionTestUtils.setField(member, "id", id);
    return member;
  }

  private OAuthAccount createSocialAccount(Member member) {
    return OAuthAccount.builder()
        .oauthProvider(provider)
        .providerId(providerId)
        .member(member)
        .build();
  }
}
