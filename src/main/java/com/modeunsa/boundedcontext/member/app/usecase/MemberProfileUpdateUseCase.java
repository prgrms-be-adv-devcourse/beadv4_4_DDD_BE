package com.modeunsa.boundedcontext.member.app.usecase;

import com.modeunsa.boundedcontext.member.app.support.MemberSupport;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.entity.MemberProfile;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.member.dto.request.MemberProfileUpdateRequest;
import com.modeunsa.shared.member.event.MemberProfileUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberProfileUpdateUseCase {
  private final MemberSupport memberSupport;
  private final SpringDomainEventPublisher eventPublisher;

  public void execute(Long memberId, MemberProfileUpdateRequest request) {
    Member member = memberSupport.getMember(memberId);
    MemberProfile profile = member.getProfile();

    if (profile == null) {
      throw new GeneralException(ErrorStatus.MEMBER_PROFILE_NOT_FOUND);
    } else {
      profile
          .updateNickname(request.nickname())
          .updateProfileImageUrl(request.profileImageUrl())
          .updateHeightCm(request.heightCm())
          .updateWeightKg(request.weightKg())
          .updateSkinType(request.skinType());
    }

    eventPublisher.publish(
        new MemberProfileUpdatedEvent(
            memberId,
            profile.getId(),
            profile.getNickname(),
            profile.getProfileImageUrl(),
            profile.getHeightCm(),
            profile.getWeightKg(),
            profile.getSkinType()));
  }
}
