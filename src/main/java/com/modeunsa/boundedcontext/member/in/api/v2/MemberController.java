package com.modeunsa.boundedcontext.member.in.api.v2;

import com.modeunsa.boundedcontext.member.app.facade.MemberFacade;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.security.CustomUserDetails;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.member.dto.request.MemberSignupCompleteRequest;
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
@RestController("MemberV2Controller")
@RequestMapping("/api/v2/members")
@RequiredArgsConstructor
public class MemberController {
  private final MemberFacade memberFacade;

  @Operation(summary = "회원가입 완료 처리", description = "소셜 로그인 직후(PRE_ACTIVE) 추가 정보를 입력받아 정회원(ACTIVE)으로 전환합니다.")
  @PostMapping("/signup-complete")
  public ResponseEntity<ApiResponse> completeSignup(
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody @Valid MemberSignupCompleteRequest request) {

    Long memberId = Long.parseLong(userDetails.getUsername());

    memberFacade.completeSignup(memberId, request);

    return ApiResponse.onSuccess(SuccessStatus.OK); // 혹은 SuccessStatus.SIGNUP_COMPLETE 정의하여 사용
  }
}
