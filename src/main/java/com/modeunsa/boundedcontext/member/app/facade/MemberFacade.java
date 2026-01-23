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
import com.modeunsa.boundedcontext.member.app.usecase.SellerRegisterUseCase;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.entity.MemberProfile;
import com.modeunsa.global.s3.S3UploadService;
import com.modeunsa.global.s3.dto.PresignedUrlRequest;
import com.modeunsa.global.s3.dto.PresignedUrlResponse;
import com.modeunsa.global.s3.dto.PublicUrlRequest;
import com.modeunsa.global.s3.dto.PublicUrlResponse;
import com.modeunsa.shared.member.dto.request.MemberBasicInfoUpdateRequest;
import com.modeunsa.shared.member.dto.request.MemberDeliveryAddressCreateRequest;
import com.modeunsa.shared.member.dto.request.MemberDeliveryAddressUpdateRequest;
import com.modeunsa.shared.member.dto.request.MemberProfileCreateRequest;
import com.modeunsa.shared.member.dto.request.MemberProfileUpdateRequest;
import com.modeunsa.shared.member.dto.request.SellerRegisterRequest;
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
  private final S3UploadService s3UploadService;
  private final MemberProfileUpdateImageUseCase memberProfileUpdateImageUseCase;

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

  /** 판매자 관련 */
  @Transactional
  public void approveSeller(Long sellerId) {
    adminApproveSellerUseCase.execute(sellerId);
  }

  @Transactional
  public void registerSeller(Long memberId, SellerRegisterRequest request, String licenseImage) {
    sellerRegisterUseCase.execute(memberId, request, licenseImage);
  }

  /** 프로필 이미지 업로드 */
  @Transactional(readOnly = true)
  public PresignedUrlResponse issueProfilePresignedUrl(PresignedUrlRequest request) {
    return s3UploadService.issuePresignedUrl(request);
  }

  @Transactional
  public PublicUrlResponse updateProfileImage(Long memberId, PublicUrlRequest request) {
    PublicUrlResponse s3Response = s3UploadService.getPublicUrl(request);
    memberProfileUpdateImageUseCase.execute(memberId, s3Response.imageUrl());
    // TODO: 새 이미지와 다르고, 기존 이미지가 존재할 경우 삭제하여 비용 절감
    return s3Response;
  }
}
