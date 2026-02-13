package com.modeunsa.boundedcontext.auth.app.usecase;

import com.modeunsa.boundedcontext.auth.domain.entity.OAuthAccount;
import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.auth.out.repository.AuthSocialAccountRepository;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.out.repository.MemberRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.auth.dto.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthAccountBindingUseCase {

  private final MemberRepository memberRepository;
  private final AuthSocialAccountRepository socialAccountRepository;

  public void link(Long memberId, OAuthProvider provider, OAuthUserInfo userInfo) {
    // 1. 이미 다른 회원이 해당 소셜 계정을 사용 중인지 확인
    boolean alreadyUsedByOther =
        socialAccountRepository.existsByOauthProviderAndProviderId(provider, userInfo.providerId());

    if (alreadyUsedByOther) {
      throw new GeneralException(ErrorStatus.SOCIAL_ACCOUNT_ALREADY_IN_USE);
    }

    // 2. 현재 회원이 이미 해당 provider로 연동했는지 확인
    boolean alreadyLinked =
        socialAccountRepository.existsByMemberIdAndOauthProvider(memberId, provider);

    if (alreadyLinked) {
      throw new GeneralException(ErrorStatus.SOCIAL_ACCOUNT_ALREADY_LINKED);
    }

    // 3. 회원 조회
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

    // 4. 소셜 계정 엔티티 생성 및 연동
    log.info(
        "기존 회원에 소셜 계정 연동 - memberId: {}, provider: {}, providerId: {}",
        member.getId(),
        userInfo.provider(),
        userInfo.providerId());

    OAuthAccount socialAccount =
        OAuthAccount.builder()
            .oauthProvider(userInfo.provider())
            .providerId(userInfo.providerId())
            .build();

    member.addOAuthAccount(socialAccount);

    log.info(
        "소셜 계정 연동 완료 - memberId: {}, provider: {}, providerId: {}",
        memberId,
        provider,
        socialAccount.getProviderId());
  }
}
