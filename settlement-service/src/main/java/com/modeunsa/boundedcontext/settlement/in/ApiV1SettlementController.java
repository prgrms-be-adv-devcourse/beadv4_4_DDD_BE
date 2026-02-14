package com.modeunsa.boundedcontext.settlement.in;

import com.modeunsa.boundedcontext.settlement.app.SettlementFacade;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.security.CustomUserDetails;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.settlement.dto.SettlementResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Settlement", description = "정산 도메인 API")
@RestController
@RequestMapping("/api/v1/settlements")
@RequiredArgsConstructor
public class ApiV1SettlementController {
  private final SettlementFacade settlementFacade;

  @Operation(summary = "월별 정산서 조회", description = "원하는 월의 정산서를 조회합니다.")
  @GetMapping("/{year}/{month}")
  public ResponseEntity<ApiResponse> getSettlement(
      @AuthenticationPrincipal CustomUserDetails user,
      @PathVariable int year,
      @PathVariable @Min(1) @Max(12) int month) {
    Long memberId = user.getMemberId();
    SettlementResponseDto settlementResponseDto =
        settlementFacade.getSettlement(memberId, year, month);

    return ApiResponse.onSuccess(SuccessStatus.CREATED, settlementResponseDto);
  }
}
