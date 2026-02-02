package com.modeunsa.boundedcontext.auth.app.usecase;

import com.modeunsa.boundedcontext.auth.domain.entity.OAuthAccount;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.out.repository.MemberRepository;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.shared.auth.dto.OAuthUserInfo;
import com.modeunsa.shared.member.event.MemberSignupEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthMemberRegisterUseCase {

  private final MemberRepository memberRepository;
  private final EventPublisher eventPublisher;

  @Transactional
  public OAuthAccount execute(OAuthUserInfo userInfo) {
    log.info("신규 회원 가입 - provider: {}, providerId: {}", userInfo.provider(), userInfo.providerId());

    // 1. Member 생성
    Member member =
        Member.builder()
            .email(userInfo.email())
            .realName(userInfo.name())
            .phoneNumber(userInfo.phoneNumber())
            .build();

    // 2. 소셜 계정 연동 및 양방향 연관관계 설정
    OAuthAccount socialAccount =
        OAuthAccount.builder()
            .oauthProvider(userInfo.provider())
            .providerId(userInfo.providerId())
            .build();

    member.addOAuthAccount(socialAccount);

    // 3. Member를 저장하면 cascade에 의해 AuthSocialAccount도 함께 저장됨
    memberRepository.save(member);

    // 4. 회원가입 이벤트 발행
    eventPublisher.publish(
        new MemberSignupEvent(
            member.getId(),
            member.getRealName(),
            member.getEmail(),
            member.getPhoneNumber(),
            member.getRole(),
            member.getStatus()));

    return socialAccount;
  }
}
