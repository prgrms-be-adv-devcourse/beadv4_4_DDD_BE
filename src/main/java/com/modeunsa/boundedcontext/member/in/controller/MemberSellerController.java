package com.modeunsa.boundedcontext.member.in.controller;

import com.modeunsa.boundedcontext.member.app.facade.MemberFacade;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.member.dto.SellerRegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberSellerController {

  private final MemberFacade memberFacade;

  /** 판매자 등록 요청 */
  @PostMapping(value = "/sellers/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse> registerSeller(
      @AuthenticationPrincipal User user,
      @RequestPart("request") @Valid SellerRegisterRequest request,
      // TODO: MultipartFile 변경 예정
      @RequestPart(value = "licenseImage", required = true) String licenseImage) {
    String username = user.getUsername();
    Long memberId;
    try {
      memberId = Long.parseLong(username);
    } catch (NumberFormatException e) {
      throw new GeneralException(ErrorStatus.MEMBER_INVALID_ID_FORMAT);
    }

    memberFacade.registerSeller(memberId, request, licenseImage);

    return ApiResponse.onSuccess(SuccessStatus.SELLER_REGISTER_SUCCESS);
  }
}
