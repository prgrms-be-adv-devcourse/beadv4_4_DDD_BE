package com.modeunsa.boundedcontext.auth.app.usecase;

import com.modeunsa.boundedcontext.auth.domain.entity.OAuthAccount;
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
public class OAuthAccountResolveUseCase {

  private final AuthSocialAccountRepository socialAccountRepository;
  private final OAuthMemberRegisterUseCase oauthMemberRegisterUseCase;

  // OAuth 계정 조회
  public OAuthAccount execute(OAuthProvider provider, OAuthUserInfo userInfo) {
    return socialAccountRepository
        .findByOauthProviderAndProviderAccountId(provider, userInfo.providerId())
        .orElseGet(() -> registerWithDuplicateHandling(provider, userInfo));
  }

  // 동시 가입 요청에 따른 중복 처리
  private OAuthAccount registerWithDuplicateHandling(
      OAuthProvider provider, OAuthUserInfo userInfo) {
    try {
      return oauthMemberRegisterUseCase.execute(userInfo);
    } catch (DataIntegrityViolationException e) {
      log.warn(
          "동시 가입 요청 감지, 기존 계정 재조회 - provider: {}, providerId: {}",
          provider,
          userInfo.providerId());

      return socialAccountRepository
          .findByOauthProviderAndProviderAccountId(provider, userInfo.providerId())
          .orElseThrow(
              () -> {
                log.error(
                    "중복 예외 발생 후 재조회 실패 - provider: {}, providerId: {}",
                    provider,
                    userInfo.providerId());
                return new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
              });
    }
  }
}
