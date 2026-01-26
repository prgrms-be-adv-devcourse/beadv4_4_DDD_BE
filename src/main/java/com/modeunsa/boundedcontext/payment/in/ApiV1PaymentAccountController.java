package com.modeunsa.boundedcontext.payment.in;

import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentAccountDepositRequest;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentAccountDepositResponse;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.security.CustomUserDetails;
import com.modeunsa.global.status.SuccessStatus;
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

@Tag(name = "Payment Account", description = "결제 계좌 도메인 API")
@RestController
@RequestMapping("/api/v1/payments/accounts")
@RequiredArgsConstructor
public class ApiV1PaymentAccountController {

  private final PaymentFacade paymentFacade;

  @Operation(summary = "계좌 입금 기능", description = "계좌에 입금하는 기능입니다.")
  @PostMapping("/deposit")
  public ResponseEntity<ApiResponse> depositAccount(
      @AuthenticationPrincipal CustomUserDetails user,
      @Valid @RequestBody PaymentAccountDepositRequest request) {
    PaymentAccountDepositResponse response =
        paymentFacade.creditAccount(user.getMemberId(), request);
    return ApiResponse.onSuccess(SuccessStatus.OK, response);
  }
}
