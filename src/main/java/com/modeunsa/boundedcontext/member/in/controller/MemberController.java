package com.modeunsa.boundedcontext.member.in.controller;

import com.modeunsa.boundedcontext.member.app.facade.MemberFacade;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.s3.dto.DomainType;
import com.modeunsa.global.s3.dto.PresignedUrlRequest;
import com.modeunsa.global.s3.dto.PresignedUrlResponse;
import com.modeunsa.global.s3.dto.PublicUrlRequest;
import com.modeunsa.global.s3.dto.PublicUrlResponse;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.member.dto.request.MemberBasicInfoUpdateRequest;
import com.modeunsa.shared.member.dto.request.MemberDeliveryAddressCreateRequest;
import com.modeunsa.shared.member.dto.request.MemberDeliveryAddressUpdateRequest;
import com.modeunsa.shared.member.dto.request.MemberProfileCreateRequest;
import com.modeunsa.shared.member.dto.request.MemberProfileUpdateRequest;
import com.modeunsa.shared.member.dto.request.SellerRegisterRequest;
import com.modeunsa.shared.member.dto.response.MemberBasicInfoResponse;
import com.modeunsa.shared.member.dto.response.MemberDeliveryAddressResponse;
import com.modeunsa.shared.member.dto.response.MemberProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member", description = "회원 API")
@RestController
@RequestMapping("/api/members/me")
@RequiredArgsConstructor
public class MemberController {

  private final MemberFacade memberFacade;

  /** 생성 (Create) */
  @Operation(summary = "배송지 추가", description = "새로운 배송지를 추가합니다. 최대 10개까지 등록 가능합니다.")
  @PostMapping("/addresses")
  public ResponseEntity<ApiResponse> addAddress(
      @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
      @RequestBody @Valid MemberDeliveryAddressCreateRequest request) {
    memberFacade.addAddress(memberId, request);
    return ApiResponse.onSuccess(SuccessStatus.MEMBER_ADDRESS_CREATE_SUCCESS);
  }

  @Operation(summary = "프로필 생성", description = "회원 프로필을 생성합니다. 이미 존재할 경우 409 에러가 발생합니다.")
  @PostMapping("/profile")
  public ResponseEntity<ApiResponse> createProfile(
      @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
      @RequestBody @Valid MemberProfileCreateRequest request) {
    memberFacade.createProfile(memberId, request);
    return ApiResponse.onSuccess(SuccessStatus.MEMBER_PROFILE_CREATE_SUCCESS);
  }

  /** 조회 (Read) */
  @Operation(summary = "기본 정보 조회", description = "회원의 이름, 전화번호, 이메일 등 기본 정보를 조회합니다.")
  @GetMapping("/basic-info")
  public ResponseEntity<ApiResponse> getMemberInfo(
      @Parameter(hidden = true) @AuthenticationPrincipal Long memberId) {
    MemberBasicInfoResponse response = memberFacade.getMemberInfo(memberId);
    return ApiResponse.onSuccess(SuccessStatus.MEMBER_BASIC_INFO_GET_SUCCESS, response);
  }

  @Operation(
      summary = "프로필 조회",
      description = "회원의 닉네임, 프로필 이미지 등 프로필 정보를 조회합니다. 프로필이 없으면 404 에러가 발생합니다.")
  @GetMapping("/profile")
  public ResponseEntity<ApiResponse> getProfile(
      @Parameter(hidden = true) @AuthenticationPrincipal Long memberId) {
    MemberProfileResponse response = memberFacade.getMemberProfile(memberId);
    return ApiResponse.onSuccess(SuccessStatus.MEMBER_PROFILE_GET_SUCCESS, response);
  }

  @Operation(summary = "배송지 목록 조회", description = "등록된 모든 배송지 목록을 조회합니다.")
  @GetMapping("/addresses")
  public ResponseEntity<ApiResponse> getDeliveryAddresses(
      @Parameter(hidden = true) @AuthenticationPrincipal Long memberId) {
    List<MemberDeliveryAddressResponse> response =
        memberFacade.getMemberDeliveryAddresses(memberId);
    return ApiResponse.onSuccess(SuccessStatus.MEMBER_ADDRESS_LIST_GET_SUCCESS, response);
  }

  /** 수정 (Update) */
  @Operation(summary = "기본 정보 수정", description = "회원의 이름, 전화번호 등 기본 정보를 수정합니다.")
  @PatchMapping("/basic-info")
  public ResponseEntity<ApiResponse> updateBasicInfo(
      @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
      @RequestBody @Valid MemberBasicInfoUpdateRequest request) {
    memberFacade.updateBasicInfo(memberId, request);
    return ApiResponse.onSuccess(SuccessStatus.MEMBER_BASIC_INFO_UPDATE_SUCCESS);
  }

  @Operation(summary = "프로필 수정", description = "회원의 닉네임, 키, 몸무게 등 프로필 정보를 수정합니다.")
  @PutMapping("/profile")
  public ResponseEntity<ApiResponse> updateProfile(
      @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
      @RequestBody @Valid MemberProfileUpdateRequest request) {
    memberFacade.updateProfile(memberId, request);
    return ApiResponse.onSuccess(SuccessStatus.MEMBER_PROFILE_UPDATE_SUCCESS);
  }

  @Operation(summary = "배송지 상세 수정", description = "특정 배송지의 수령인, 주소, 연락처 정보를 수정합니다.")
  @PatchMapping("/addresses/{addressId}")
  public ResponseEntity<ApiResponse> updateAddress(
      @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
      @PathVariable Long addressId,
      @RequestBody @Valid MemberDeliveryAddressUpdateRequest request) {
    memberFacade.updateDeliveryAddress(memberId, addressId, request);
    return ApiResponse.onSuccess(SuccessStatus.MEMBER_ADDRESS_UPDATE_SUCCESS);
  }

  @Operation(summary = "기본 배송지 설정", description = "특정 배송지를 기본 배송지로 설정합니다. 기존 기본 배송지는 자동으로 해제됩니다.")
  @PatchMapping("/addresses/{addressId}/default")
  public ResponseEntity<ApiResponse> setDefaultAddress(
      @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
      @PathVariable Long addressId) {
    memberFacade.setDefaultAddress(memberId, addressId);
    return ApiResponse.onSuccess(SuccessStatus.MEMBER_DEFAULT_ADDRESS_UPDATE_SUCCESS);
  }

  /** 삭제 (Delete) */
  @Operation(summary = "배송지 삭제", description = "특정 배송지를 삭제합니다.")
  @DeleteMapping("/addresses/{addressId}")
  public ResponseEntity<ApiResponse> deleteAddress(
      @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
      @PathVariable Long addressId) {
    memberFacade.deleteDeliveryAddress(memberId, addressId);
    return ApiResponse.onSuccess(SuccessStatus.MEMBER_ADDRESS_DELETE_SUCCESS);
  }

  /** 판매자 등록 요청 */
  @Operation(summary = "사업자등록증 업로드 URL 발급", description = "S3 업로드를 위한 Presigned URL을 요청합니다.")
  @PostMapping("/sellers/license/presigned-url")
  public ResponseEntity<ApiResponse> getLicensePresignedUrl(
      @Parameter(hidden = true) @AuthenticationPrincipal Long memberId, @RequestBody @Valid PresignedUrlRequest request) {

    PresignedUrlRequest secureRequest =
        new PresignedUrlRequest(memberId, DomainType.SELLER, request.ext(), request.contentType());

    PresignedUrlResponse response = memberFacade.issueSellerLicensePresignedUrl(secureRequest);
    return ApiResponse.onSuccess(SuccessStatus.FILE_UPLOAD_URL_GET_SUCCESS, response);
  }

  @Operation(summary = "판매자 등록 요청", description = "업로드된 사업자등록증 키(rawKey)를 포함하여 판매자 등록을 요청합니다.")
  @PostMapping("/sellers/register")
  public ResponseEntity<ApiResponse> registerSeller(
      @Parameter(hidden = true) @AuthenticationPrincipal Long memberId, @RequestBody @Valid SellerRegisterRequest request) {

    memberFacade.registerSeller(memberId, request);

    return ApiResponse.onSuccess(SuccessStatus.SELLER_REGISTER_SUCCESS);
  }

  /** 프로필 이미지 업로드 */
  @Operation(summary = "프로필 이미지 업로드 URL 발급", description = "S3 업로드를 위한 Presigned URL을 요청합니다.")
  @PostMapping("/profile/presigned-url")
  public ResponseEntity<ApiResponse> getPresignedUrl(
      @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
      @RequestBody @Valid PresignedUrlRequest request) {

    PresignedUrlRequest secureRequest =
        new PresignedUrlRequest(memberId, DomainType.MEMBER, request.ext(), request.contentType());

    PresignedUrlResponse response = memberFacade.issueProfilePresignedUrl(secureRequest);
    return ApiResponse.onSuccess(SuccessStatus.FILE_UPLOAD_URL_GET_SUCCESS, response);
  }

  @Operation(summary = "프로필 이미지 적용", description = "S3 업로드 완료 후, 해당 이미지를 실제 프로필로 적용합니다.")
  @PatchMapping("/profile/image")
  public ResponseEntity<ApiResponse> updateProfileImage(
      @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
      @RequestBody @Valid PublicUrlRequest request) {

    PublicUrlRequest secureRequest =
        new PublicUrlRequest(request.rawKey(), DomainType.MEMBER, memberId, request.contentType());

    PublicUrlResponse response = memberFacade.updateProfileImage(memberId, secureRequest);
    return ApiResponse.onSuccess(SuccessStatus.MEMBER_PROFILE_UPDATE_SUCCESS, response);
  }
}
