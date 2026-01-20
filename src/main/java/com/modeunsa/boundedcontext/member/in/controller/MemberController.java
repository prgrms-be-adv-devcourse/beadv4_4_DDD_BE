package com.modeunsa.boundedcontext.member.in.controller;

import com.modeunsa.boundedcontext.member.app.facade.MemberFacade;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.member.dto.request.MemberBasicInfoUpdateRequest;
import com.modeunsa.shared.member.dto.request.MemberDeliveryAddressCreateRequest;
import com.modeunsa.shared.member.dto.request.MemberDeliveryAddressUpdateRequest;
import com.modeunsa.shared.member.dto.request.MemberProfileCreateRequest;
import com.modeunsa.shared.member.dto.request.MemberProfileUpdateRequest;
import com.modeunsa.shared.member.dto.response.MemberBasicInfoResponse;
import com.modeunsa.shared.member.dto.response.MemberDeliveryAddressResponse;
import com.modeunsa.shared.member.dto.response.MemberProfileResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members/me")
@RequiredArgsConstructor
public class MemberController {

  private final MemberFacade memberFacade;

  /** 생성 (Create) */
  @PostMapping("/addresses")
  public ResponseEntity<ApiResponse> addAddress(
      @AuthenticationPrincipal UserDetails user,
      @RequestBody MemberDeliveryAddressCreateRequest request) {
    memberFacade.addAddress(Long.valueOf(user.getUsername()), request);
    return ApiResponse.onSuccess(SuccessStatus.MEMBER_ADDRESS_CREATE_SUCCESS);
  }

  @PostMapping("/profile")
  public ResponseEntity<ApiResponse> createProfile(
      @AuthenticationPrincipal UserDetails user, @RequestBody MemberProfileCreateRequest request) {
    memberFacade.createProfile(Long.valueOf(user.getUsername()), request);
    return ApiResponse.onSuccess(SuccessStatus.MEMBER_PROFILE_CREATE_SUCCESS);
  }

  /** 조회 (Read) */
  @GetMapping("/basic-info")
  public ResponseEntity<ApiResponse> getMemberInfo(@AuthenticationPrincipal UserDetails user) {
    MemberBasicInfoResponse response = memberFacade.getMemberInfo(Long.valueOf(user.getUsername()));
    return ApiResponse.onSuccess(SuccessStatus.MEMBER_BASIC_INFO_GET_SUCCESS, response);
  }

  @GetMapping("/profile")
  public ResponseEntity<ApiResponse> getProfile(@AuthenticationPrincipal UserDetails user) {
    MemberProfileResponse response =
        memberFacade.getMemberProfile(Long.valueOf(user.getUsername()));
    return ApiResponse.onSuccess(SuccessStatus.MEMBER_PROFILE_GET_SUCCESS, response);
  }

  @GetMapping("/addresses")
  public ResponseEntity<ApiResponse> getDeliveryAddresses(
      @AuthenticationPrincipal UserDetails user) {
    List<MemberDeliveryAddressResponse> response =
        memberFacade.getMemberAddresses(Long.valueOf(user.getUsername()));
    return ApiResponse.onSuccess(SuccessStatus.MEMBER_ADDRESS_LIST_GET_SUCCESS, response);
  }

  /** 수정 (Update) */
  @PatchMapping("/basic-info")
  public ResponseEntity<ApiResponse> updateBasicInfo(
      @AuthenticationPrincipal UserDetails user,
      @RequestBody @Valid MemberBasicInfoUpdateRequest request) {
    memberFacade.updateBasicInfo(Long.valueOf(user.getUsername()), request);
    return ApiResponse.onSuccess(SuccessStatus.MEMBER_BASIC_INFO_UPDATE_SUCCESS);
  }

  @PutMapping("/profile")
  public ResponseEntity<ApiResponse> updateProfile(
      @AuthenticationPrincipal UserDetails user, @RequestBody MemberProfileUpdateRequest request) {
    memberFacade.updateProfile(Long.valueOf(user.getUsername()), request);
    return ApiResponse.onSuccess(SuccessStatus.MEMBER_PROFILE_UPDATE_SUCCESS);
  }

  @PatchMapping("/addresses/{addressId}")
  public ResponseEntity<ApiResponse> updateAddress(
      @AuthenticationPrincipal UserDetails user,
      @PathVariable Long addressId,
      @RequestBody @Valid MemberDeliveryAddressUpdateRequest request
  ) {
    memberFacade.updateDeliveryAddress(Long.valueOf(user.getUsername()), addressId, request);
    return ApiResponse.onSuccess(SuccessStatus.MEMBER_ADDRESS_UPDATE_SUCCESS);
  }

  @PatchMapping("/addresses/{addressId}/default")
  public ResponseEntity<ApiResponse> setDefaultAddress(
      @AuthenticationPrincipal UserDetails user, @PathVariable Long addressId) {
    memberFacade.setDefaultAddress(Long.valueOf(user.getUsername()), addressId);
    return ApiResponse.onSuccess(SuccessStatus.MEMBER_DEFAULT_ADDRESS_UPDATE_SUCCESS);
  }

  /** 삭제 (Delete) */
  @DeleteMapping("/addresses/{addressId}")
  public ResponseEntity<ApiResponse> deleteAddress(
      @AuthenticationPrincipal UserDetails user, @PathVariable Long addressId) {
    memberFacade.deleteDeliveryAddress(Long.valueOf(user.getUsername()), addressId);
    return ApiResponse.onSuccess(SuccessStatus.MEMBER_ADDRESS_DELETE_SUCCESS);
  }
}
