package com.modeunsa.boundedcontext.member.app.usecase;

import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.entity.MemberProfile;
import com.modeunsa.boundedcontext.member.out.repository.MemberRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberProfileUpdateImageUseCase {
  private final MemberRepository memberRepository;

  /**
   * 비즈니스 행위: 회원의 프로필 이미지 URL을 변경한다.
   * @return 변경되기 전의 기존 이미지 URL (S3 삭제 처리를 위함)
   */
  public String execute(Long memberId, String newImageUrl) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

    MemberProfile profile = member.getProfile();

    if (profile == null) {
      throw new GeneralException(ErrorStatus.MEMBER_PROFILE_NOT_FOUND);
    }

    // 기존 URL 백업 (삭제용)
    String oldImageUrl = profile.getProfileImageUrl();

    // 상태 변경
    profile.updateProfileImageUrl(newImageUrl);

    return oldImageUrl;
  }

}
