package com.modeunsa.boundedcontext.payment.in.api.v1;

import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberDto;
import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberResponse;
import com.modeunsa.boundedcontext.payment.app.mapper.PaymentMapper;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.security.CustomUserDetails;
import com.modeunsa.global.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment", description = "결제 도메인 API")
@RestController("PaymentMemberV1Controller")
@RequestMapping("/api/v1/payments/members")
@RequiredArgsConstructor
public class PaymentMemberController {

  private final PaymentFacade paymentFacade;
  private final PaymentMapper paymentMapper;

  @Operation(summary = "결제 회원 정보 조회 기능", description = "결제 회원 정보를 조회하는 기능입니다.")
  @GetMapping
  public ResponseEntity<ApiResponse<PaymentMemberResponse>> getMember(
      @AuthenticationPrincipal CustomUserDetails user) {
    PaymentMemberDto data = paymentFacade.getMember(user.getMemberId());
    PaymentMemberResponse response = paymentMapper.toPaymentMemberResponse(data);
    return ApiResponse.onSuccessTyped(SuccessStatus.OK, response);
  }
}
