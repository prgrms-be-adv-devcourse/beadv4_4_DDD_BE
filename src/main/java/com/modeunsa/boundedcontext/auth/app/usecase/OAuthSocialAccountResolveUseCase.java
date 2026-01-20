package com.modeunsa.boundedcontext.auth.app.usecase;

import com.modeunsa.boundedcontext.auth.domain.entity.AuthSocialAccount;
import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.auth.out.repository.AuthSocialAccountRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.auth.dto.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthSocialAccountResolveUseCase {

  private final AuthSocialAccountRepository socialAccountRepository;
  private final OAuthMemberRegisterUseCase oauthMemberRegisterUseCase;

  public AuthSocialAccount execute(OAuthProvider provider, OAuthUserInfo userInfo) {
    return socialAccountRepository
        .findByOauthProviderAndProviderAccountId(provider, userInfo.getProviderId())
        .orElseGet(() -> registerWithDuplicateHandling(provider, userInfo));
  }

  private AuthSocialAccount registerWithDuplicateHandling(
      OAuthProvider provider, OAuthUserInfo userInfo) {
    try {
      return oauthMemberRegisterUseCase.execute(userInfo);
    } catch (DataIntegrityViolationException e) {
      log.warn(
          "동시 가입 요청 감지, 기존 계정 재조회 - provider: {}, providerId: {}",
          provider,
          userInfo.getProviderId());

      return socialAccountRepository
          .findByOauthProviderAndProviderAccountId(provider, userInfo.getProviderId())
          .orElseThrow(
              () -> {
                log.error(
                    "중복 예외 발생 후 재조회 실패 - provider: {}, providerId: {}",
                    provider,
                    userInfo.getProviderId());
                return new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
              });
    }
  }
}
