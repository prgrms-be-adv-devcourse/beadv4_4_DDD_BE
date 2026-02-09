package com.modeunsa.boundedcontext.member.app.usecase;

import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.types.MemberStatus;
import com.modeunsa.boundedcontext.member.out.repository.MemberProfileRepository;
import com.modeunsa.boundedcontext.member.out.repository.MemberRepository;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.member.dto.request.MemberBasicInfoUpdateRequest;
import com.modeunsa.shared.member.dto.request.MemberProfileCreateRequest;
import com.modeunsa.shared.member.dto.request.MemberSignupCompleteRequest;
import com.modeunsa.shared.member.event.MemberSignupEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberSignupCompleteUseCase {
  private final MemberRepository memberRepository;
  private final MemberProfileRepository memberProfileRepository;
  private final EventPublisher eventPublisher;
  private final MemberProfileCreateUseCase memberProfileCreateUseCase;
  private final MemberBasicInfoUpdateUseCase memberBasicInfoUpdateUseCase;

  @Transactional
  public void execute(Long memberId, MemberSignupCompleteRequest request) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

    // 이미 가입 완료된 회원인지 검증
    if (member.getStatus() == MemberStatus.ACTIVE) {
      return;
    }

    // 1. 기본 정보 업데이트
    MemberBasicInfoUpdateRequest memberBasicInfoUpdateRequest = new MemberBasicInfoUpdateRequest(request.realName(), request.phoneNumber(), request.email());
    memberBasicInfoUpdateUseCase.execute(memberId, memberBasicInfoUpdateRequest);

    // 2. 프로필 생성 및 저장
    MemberProfileCreateRequest memberProfileCreateRequest = new MemberProfileCreateRequest(request.nickname(),
        request.profileImageUrl(), request.heightCm(), request.weightKg(), request.skinType());
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
            member.getStatus().name()
        )
    );
  }
}
