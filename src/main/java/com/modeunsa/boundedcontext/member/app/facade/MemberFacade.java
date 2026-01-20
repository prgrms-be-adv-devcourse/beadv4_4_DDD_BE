package com.modeunsa.boundedcontext.member.app.facade;

import com.modeunsa.boundedcontext.member.app.support.MemberSupport;
import com.modeunsa.boundedcontext.member.app.usecase.MemberBasicInfoUpdateUseCase;
import com.modeunsa.boundedcontext.member.app.usecase.MemberDeliveryAddressAddUseCase;
import com.modeunsa.boundedcontext.member.app.usecase.MemberDeliveryAddressDeleteUseCase;
import com.modeunsa.boundedcontext.member.app.usecase.MemberDeliveryAddressSetDefaultUseCase;
import com.modeunsa.boundedcontext.member.app.usecase.MemberProfileCreateUseCase;
import com.modeunsa.boundedcontext.member.app.usecase.MemberProfileUpdateUseCase;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.entity.MemberProfile;
import com.modeunsa.shared.member.dto.request.MemberBasicInfoUpdateRequest;
import com.modeunsa.shared.member.dto.request.MemberDeliveryAddressCreateRequest;
import com.modeunsa.shared.member.dto.request.MemberProfileCreateRequest;
import com.modeunsa.shared.member.dto.request.MemberProfileUpdateRequest;
import com.modeunsa.shared.member.dto.response.MemberBasicInfoResponse;
import com.modeunsa.shared.member.dto.response.MemberDeliveryAddressResponse;
import com.modeunsa.shared.member.dto.response.MemberProfileResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberFacade {

  private final MemberBasicInfoUpdateUseCase memberBasicInfoUpdateUseCase;
  private final MemberProfileCreateUseCase memberProfileCreateUseCase;
  private final MemberProfileUpdateUseCase memberProfileUpdateUseCase;
  private final MemberDeliveryAddressAddUseCase memberDeliveryAddressAddUseCase;
  private final MemberDeliveryAddressSetDefaultUseCase memberDeliveryAddressSetDefaultUseCase;
  private final MemberDeliveryAddressDeleteUseCase memberDeliveryAddressDeleteUseCase;
  private final MemberSupport memberSupport;

  /** 생성 (Create) */
  @Transactional
  public void createProfile(Long memberId, MemberProfileCreateRequest request) {
    memberProfileCreateUseCase.execute(memberId, request);
  }

  @Transactional
  public void addAddress(Long memberId, MemberDeliveryAddressCreateRequest request) {
    memberDeliveryAddressAddUseCase.execute(memberId, request);
  }

  /** 조회 (Read) */
  @Transactional(readOnly = true)
  public MemberBasicInfoResponse getMemberInfo(Long memberId) {
    Member member = memberSupport.getMember(memberId);
    return MemberBasicInfoResponse.from(member);
  }

  @Transactional(readOnly = true)
  public MemberProfileResponse getMemberProfile(Long memberId) {
    MemberProfile profile = memberSupport.getMemberProfileOrThrow(memberId);
    return MemberProfileResponse.from(profile);
  }

  @Transactional(readOnly = true)
  public List<MemberDeliveryAddressResponse> getMemberAddresses(Long memberId) {
    Member member = memberSupport.getMember(memberId);
    return member.getAddresses().stream()
        .map(MemberDeliveryAddressResponse::from)
        .collect(Collectors.toList());
  }

  /** 수정 (Update) */
  @Transactional
  public void updateBasicInfo(Long memberId, MemberBasicInfoUpdateRequest request) {
    memberBasicInfoUpdateUseCase.execute(memberId, request);
  }

  @Transactional
  public void updateProfile(Long memberId, MemberProfileUpdateRequest request) {
    memberProfileUpdateUseCase.execute(memberId, request);
  }

  @Transactional
  public void setDefaultAddress(Long memberId, Long addressId) {
    memberDeliveryAddressSetDefaultUseCase.execute(memberId, addressId);
  }

  /** 삭제 (Delete) */
  @Transactional
  public void deleteDeliveryAddress(Long memberId, Long addressId) {
    memberDeliveryAddressDeleteUseCase.execute(memberId, addressId);
  }
}
