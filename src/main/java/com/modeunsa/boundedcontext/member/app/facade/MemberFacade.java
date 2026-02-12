package com.modeunsa.boundedcontext.member.app.facade;

import com.modeunsa.boundedcontext.member.app.support.MemberSupport;
import com.modeunsa.boundedcontext.member.app.usecase.AdminApproveSellerUseCase;
import com.modeunsa.boundedcontext.member.app.usecase.MemberBasicInfoUpdateUseCase;
import com.modeunsa.boundedcontext.member.app.usecase.MemberDeliveryAddressAddUseCase;
import com.modeunsa.boundedcontext.member.app.usecase.MemberDeliveryAddressDeleteUseCase;
import com.modeunsa.boundedcontext.member.app.usecase.MemberDeliveryAddressSetAsDefaultUseCase;
import com.modeunsa.boundedcontext.member.app.usecase.MemberDeliveryAddressUpdateUseCase;
import com.modeunsa.boundedcontext.member.app.usecase.MemberProfileCreateUseCase;
import com.modeunsa.boundedcontext.member.app.usecase.MemberProfileUpdateImageUseCase;
import com.modeunsa.boundedcontext.member.app.usecase.MemberProfileUpdateUseCase;
import com.modeunsa.boundedcontext.member.app.usecase.MemberSignupCompleteUseCase;
import com.modeunsa.boundedcontext.member.app.usecase.SellerRegisterUseCase;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.entity.MemberProfile;
import com.modeunsa.global.security.jwt.JwtTokenProvider;
import com.modeunsa.shared.auth.dto.JwtTokenResponse;
import com.modeunsa.shared.member.dto.request.MemberBasicInfoUpdateRequest;
import com.modeunsa.shared.member.dto.request.MemberDeliveryAddressCreateRequest;
import com.modeunsa.shared.member.dto.request.MemberDeliveryAddressUpdateRequest;
import com.modeunsa.shared.member.dto.request.MemberProfileCreateRequest;
import com.modeunsa.shared.member.dto.request.MemberProfileUpdateRequest;
import com.modeunsa.shared.member.dto.request.MemberSignupCompleteRequest;
import com.modeunsa.shared.member.dto.request.SellerRegisterRequest;
import com.modeunsa.shared.member.dto.response.MemberBasicInfoResponse;
import com.modeunsa.shared.member.dto.response.MemberDeliveryAddressResponse;
import com.modeunsa.shared.member.dto.response.MemberProfileResponse;
import com.modeunsa.shared.member.dto.response.SellerRegisterResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberFacade {
  private final AdminApproveSellerUseCase adminApproveSellerUseCase;
  private final SellerRegisterUseCase sellerRegisterUseCase;
  private final MemberBasicInfoUpdateUseCase memberBasicInfoUpdateUseCase;
  private final MemberProfileCreateUseCase memberProfileCreateUseCase;
  private final MemberProfileUpdateUseCase memberProfileUpdateUseCase;
  private final MemberDeliveryAddressAddUseCase memberDeliveryAddressAddUseCase;
  private final MemberDeliveryAddressUpdateUseCase memberDeliveryAddressUpdateUseCase;
  private final MemberDeliveryAddressSetAsDefaultUseCase memberDeliveryAddressSetAsDefaultUseCase;
  private final MemberDeliveryAddressDeleteUseCase memberDeliveryAddressDeleteUseCase;
  private final MemberSupport memberSupport;
  private final MemberProfileUpdateImageUseCase memberProfileUpdateImageUseCase;
  private final JwtTokenProvider jwtTokenProvider;
  private final MemberSignupCompleteUseCase memberSignupCompleteUseCase;

  /** 회원 가입 */
  @Transactional
  public JwtTokenResponse completeSignup(Long memberId, MemberSignupCompleteRequest request) {
    return memberSignupCompleteUseCase.execute(memberId, request);
  }

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
  public List<MemberDeliveryAddressResponse> getMemberDeliveryAddresses(Long memberId) {
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
  public void updateDeliveryAddress(
      Long memberId, Long addressId, MemberDeliveryAddressUpdateRequest request) {
    memberDeliveryAddressUpdateUseCase.execute(memberId, addressId, request);
  }

  @Transactional
  public void setDefaultAddress(Long memberId, Long addressId) {
    memberDeliveryAddressSetAsDefaultUseCase.execute(memberId, addressId);
  }

  /** 삭제 (Delete) */
  @Transactional
  public void deleteDeliveryAddress(Long memberId, Long addressId) {
    memberDeliveryAddressDeleteUseCase.execute(memberId, addressId);
  }

  /** 관리자 관련 */
  @Transactional
  public void approveSeller(Long sellerId) {
    adminApproveSellerUseCase.execute(sellerId);
  }

  /** 회원 프로필 이미지 관련 */
  @Transactional
  public void updateProfileImage(Long memberId, String imageUrl) {
    memberProfileUpdateImageUseCase.execute(memberId, imageUrl);
    // TODO: 새 이미지와 다르고, 기존 이미지가 존재할 경우 삭제하여 비용 절감
  }

  /** 판매자 등록 관련 */
  @Transactional
  public SellerRegisterResponse registerSeller(Long memberId, SellerRegisterRequest request) {
    sellerRegisterUseCase.execute(memberId, request, request.businessLicenseUrl());

    Member member = memberSupport.getMember(memberId);
    Long sellerId = memberSupport.getSellerIdByMemberId(memberId);

    String accessToken =
        jwtTokenProvider.createAccessToken(
            member.getId(), member.getRole(), sellerId, member.getStatus().name());
    String refreshToken =
        jwtTokenProvider.createRefreshToken(
            member.getId(), member.getRole(), sellerId, member.getStatus().name());

    return new SellerRegisterResponse(accessToken, refreshToken);
  }
}
