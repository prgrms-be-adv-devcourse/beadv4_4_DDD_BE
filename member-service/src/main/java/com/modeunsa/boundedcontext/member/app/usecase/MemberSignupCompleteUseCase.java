package com.modeunsa.boundedcontext.member.app.usecase;

import com.modeunsa.boundedcontext.auth.app.usecase.AuthTokenIssueUseCase;
import com.modeunsa.boundedcontext.auth.domain.dto.JwtTokenResponse;
import com.modeunsa.boundedcontext.member.app.support.MemberSupport;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.out.repository.MemberRepository;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.member.MemberStatus;
import com.modeunsa.boundedcontext.member.domain.dto.request.MemberBasicInfoUpdateRequest;
import com.modeunsa.boundedcontext.member.domain.dto.request.MemberProfileCreateRequest;
import com.modeunsa.boundedcontext.member.domain.dto.request.MemberSignupCompleteRequest;
import com.modeunsa.shared.member.event.MemberSignupEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberSignupCompleteUseCase {

  private final MemberRepository memberRepository;
  private final EventPublisher eventPublisher;
  private final MemberProfileCreateUseCase memberProfileCreateUseCase;
  private final MemberBasicInfoUpdateUseCase memberBasicInfoUpdateUseCase;
  private final MemberSupport memberSupport;
  private final AuthTokenIssueUseCase authTokenIssueUseCase;

  @Transactional
  public JwtTokenResponse execute(Long memberId, MemberSignupCompleteRequest request) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

    // 가입 대기인 회원만 회원 가입 완료 가능
    if (member.getStatus() != MemberStatus.PRE_ACTIVE) {
      throw new GeneralException(ErrorStatus.MEMBER_ALREADY_EXISTS);
    }

    // 1. 기본 정보 업데이트
    MemberBasicInfoUpdateRequest memberBasicInfoUpdateRequest =
        new MemberBasicInfoUpdateRequest(
            request.realName(), request.phoneNumber(), request.email());
    memberBasicInfoUpdateUseCase.execute(memberId, memberBasicInfoUpdateRequest);

    // 2. 프로필 생성 및 저장
    MemberProfileCreateRequest memberProfileCreateRequest =
        new MemberProfileCreateRequest(
            request.nickname(),
            request.profileImageUrl(),
            request.heightCm(),
            request.weightKg(),
            request.skinType());
    memberProfileCreateUseCase.execute(memberId, memberProfileCreateRequest);

    // 3. 상태 변경 (PRE_ACTIVE -> ACTIVE)
    member.activate();

    // 4. 회원가입 이벤트 발행 (이제야 비로소 발행!)
    eventPublisher.publish(
        new MemberSignupEvent(
            member.getId(),
            member.getRealName(),
            member.getEmail(),
            member.getPhoneNumber(),
            member.getRole().name(),
            member.getStatus().name()));

    // 변경된 상태(ACTIVE)로 새 토큰 발급을 위해 회원 정보 조회
    Long sellerId = memberSupport.getSellerIdByMemberId(memberId);

    // 새 토큰 발급 (ACTIVE 상태가 들어감)
    return authTokenIssueUseCase.execute(
        member.getId(), member.getRole(), sellerId, member.getStatus().name());
  }
}
