package com.modeunsa.boundedcontext.member.in.api.v2;

import com.modeunsa.boundedcontext.member.app.facade.MemberSellerFacade;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.security.CustomUserDetails;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.member.dto.response.SellerInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member", description = "회원 API")
@RestController("MemberSellerV2Controller")
@RequestMapping("/api/v2/members")
@RequiredArgsConstructor
public class MemberSellerController {
  private final MemberSellerFacade memberSellerFacade;

  @Operation(summary = "판매자 정보 조회", description = "판매자 등록 정보를 조회합니다.")
  @GetMapping("/seller")
  public ResponseEntity<ApiResponse> getSellerInfo(
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user) {

    Long memberId = user.getMemberId();
    SellerInfoResponse response = memberSellerFacade.getSellerInfo(memberId);

    return ApiResponse.onSuccess(SuccessStatus.SELLER_INFO_GET_SUCCESS, response);
  }
}
