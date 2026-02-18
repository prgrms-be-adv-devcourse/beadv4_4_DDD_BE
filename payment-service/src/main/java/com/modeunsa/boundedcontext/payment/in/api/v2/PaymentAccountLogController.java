package com.modeunsa.boundedcontext.payment.in.api.v2;

import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.accountlog.PaymentAccountLedgerPageResponse;
import com.modeunsa.boundedcontext.payment.app.dto.accountlog.PaymentAccountLogDto;
import com.modeunsa.boundedcontext.payment.app.dto.accountlog.PaymentAccountSearchRequest;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.security.CustomUserDetails;
import com.modeunsa.global.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment Account Log", description = "결제 계좌 입출금 내역 도메인 API")
@RestController("PaymentAccountLogV2Controller")
@RequestMapping("/api/v1/payments/accounts/logs")
@RequiredArgsConstructor
public class PaymentAccountLogController {

  private final PaymentFacade paymentFacade;

  @Operation(summary = "결제 계좌 입출금 내역 조회 기능", description = "결제 계좌 입출금 내역 정보를 조회하는 기능입니다.")
  @GetMapping
  public ResponseEntity<ApiResponse> getAccountLedgePage(
      @AuthenticationPrincipal CustomUserDetails user,
      @Valid PaymentAccountSearchRequest paymentAccountSearchRequest) {
    Page<PaymentAccountLogDto> page =
        paymentFacade.getAccountLogPageListBySearch(
            user.getMemberId(), paymentAccountSearchRequest);
    Page<PaymentAccountLedgerPageResponse> response =
        page.map(PaymentAccountLedgerPageResponse::from);
    return ApiResponse.onSuccess(SuccessStatus.OK, response);
  }
}
