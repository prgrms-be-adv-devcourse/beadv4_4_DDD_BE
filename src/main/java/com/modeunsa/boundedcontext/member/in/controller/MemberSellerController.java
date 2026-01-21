package com.modeunsa.boundedcontext.member.in.controller;

import com.modeunsa.boundedcontext.member.app.facade.MemberFacade;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.member.dto.request.SellerRegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Seller", description = "판매자 관련 API")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberSellerController {

  private final MemberFacade memberFacade;

  /** 판매자 등록 요청 */
  @Operation(summary = "판매자 등록 요청", description = "일반 회원이 판매자로 전환하기 위해 등록을 요청합니다.")
  @PostMapping(value = "/sellers/register")
  public ResponseEntity<ApiResponse> registerSeller(
      @AuthenticationPrincipal Long memberId, @RequestBody @Valid SellerRegisterRequest request
  // TODO: MultipartFile 변경 예정
  ) {
    memberFacade.registerSeller(memberId, request, request.licenseImage());

    return ApiResponse.onSuccess(SuccessStatus.SELLER_REGISTER_SUCCESS);
  }
}