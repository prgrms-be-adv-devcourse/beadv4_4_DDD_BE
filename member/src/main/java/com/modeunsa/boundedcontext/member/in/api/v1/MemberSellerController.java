package com.modeunsa.boundedcontext.member.in.api.v1;

import com.modeunsa.boundedcontext.member.app.facade.MemberSellerFacade;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.security.CustomUserDetails;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.member.dto.request.SellerRegisterRequest;
import com.modeunsa.shared.member.dto.response.SellerRegisterResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member", description = "회원 API")
@RestController("MemberSellerV1Controller")
@RequestMapping("/api/v1/members/me")
@RequiredArgsConstructor
public class MemberSellerController {
  private final MemberSellerFacade memberSellerFacade;

  /** 판매자 등록 요청 */
  @Operation(summary = "판매자 등록 요청", description = "판매자 등록을 요청합니다.")
  @PostMapping("/seller/register")
  public ResponseEntity<ApiResponse> registerSeller(
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user,
      @RequestBody @Valid SellerRegisterRequest request) {

    Long memberId = user.getMemberId();
    SellerRegisterResponse response = memberSellerFacade.registerSeller(memberId, request);

    return ApiResponse.onSuccess(SuccessStatus.SELLER_REGISTER_SUCCESS, response);
  }
}
