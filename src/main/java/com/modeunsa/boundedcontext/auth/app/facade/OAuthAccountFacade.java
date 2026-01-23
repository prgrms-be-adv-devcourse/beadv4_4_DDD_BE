package com.modeunsa.boundedcontext.auth.app.facade;

import com.modeunsa.boundedcontext.auth.app.usecase.OAuthAccountLinkUseCase;
import com.modeunsa.boundedcontext.auth.app.usecase.OAuthUrlUseCase;
import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.auth.out.repository.AuthSocialAccountRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 마이페이지 소셜 계정 연동 Facade */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthAccountFacade {

  private final OAuthUrlUseCase oauthUrlUseCase;
  private final OAuthAccountLinkUseCase oauthAccountLinkUseCase;
  private final AuthSocialAccountRepository socialAccountRepository;

  /** 소셜 계정 연동용 OAuth URL 생성 */
  @Transactional(readOnly = true)
  public String getLinkUrl(Long memberId, OAuthProvider provider, String redirectUri) {
    // 이미 해당 provider로 연동된 계정이 있는지 확인
    boolean alreadyLinked =
        socialAccountRepository.existsByMemberIdAndOauthProvider(memberId, provider);

    if (alreadyLinked) {
      throw new GeneralException(ErrorStatus.SOCIAL_ACCOUNT_ALREADY_LINKED);
    }

    log.info("소셜 계정 연동 URL 생성 - memberId: {}, provider: {}", memberId, provider);

    return oauthUrlUseCase.generateOAuthUrl(provider, redirectUri);
  }

  /** 소셜 계정 연동 */
  @Transactional
  public void linkSocialAccount(
      Long memberId, OAuthProvider provider, String code, String redirectUri, String state) {
    oauthAccountLinkUseCase.execute(memberId, provider, code, redirectUri, state);
  }
}
