package com.modeunsa.boundedcontext.member.app.usecase;

import com.modeunsa.boundedcontext.member.app.support.MemberSupport;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.entity.MemberProfile;
import com.modeunsa.boundedcontext.member.out.repository.MemberProfileRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.member.dto.request.MemberProfileCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberProfileCreateUseCase {

  private final MemberSupport memberSupport;
  private final MemberProfileRepository memberProfileRepository;

  public void execute(Long memberId, MemberProfileCreateRequest request) {
    Member member = memberSupport.getMember(memberId);

    // 1. 이미 프로필이 존재하는지 검증
    if (member.getProfile() != null) {
      throw new GeneralException(ErrorStatus.MEMBER_PROFILE_ALREADY_EXISTS);
    }

    // 2. 프로필 엔티티 생성
    MemberProfile profile =
        MemberProfile.builder()
            .member(member)
            .nickname(request.nickname())
            .profileImageUrl(request.profileImageUrl())
            .heightCm(request.heightCm())
            .weightKg(request.weightKg())
            .skinType(request.skinType())
            .build();

    // 3. 연관관계 설정 및 저장
    member.setProfile(profile);
    memberProfileRepository.save(profile);
  }
}
