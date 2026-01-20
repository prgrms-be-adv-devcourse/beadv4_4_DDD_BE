package com.modeunsa.boundedcontext.payment.in;

import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberResponse;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment", description = "결제 회원 도메인 API")
@RestController
@RequestMapping("/api/v1/payments/members")
@RequiredArgsConstructor
public class ApiV1PaymentMemberController {

  private final PaymentFacade paymentFacade;

  @Operation(summary = "결제 회원 정보 조회 기능", description = "결제 회원 정보를 조회하는 기능입니다.")
  @GetMapping("/{memberId}")
  public ResponseEntity<ApiResponse> getMember(@PathVariable Long memberId) {
    PaymentMemberResponse response = paymentFacade.getMember(memberId);
    return ApiResponse.onSuccess(SuccessStatus.OK, response);
  }
}
