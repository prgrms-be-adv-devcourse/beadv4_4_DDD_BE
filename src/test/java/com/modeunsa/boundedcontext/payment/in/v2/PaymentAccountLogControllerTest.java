package com.modeunsa.boundedcontext.payment.in.v2;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.accountlog.PaymentAccountLogDto;
import com.modeunsa.boundedcontext.payment.app.dto.accountlog.PaymentAccountSearchRequest;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.boundedcontext.payment.in.BasePaymentControllerTest;
import com.modeunsa.boundedcontext.payment.in.api.v2.PaymentAccountLogController;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@DisplayName("PaymentAccountLogController 테스트")
class PaymentAccountLogControllerTest extends BasePaymentControllerTest {

  @Mock private PaymentFacade paymentFacade;

  @BeforeEach
  void setUp() {
    super.setUpBase();
    setUpMockMvc(new PaymentAccountLogController(paymentFacade));
    setSecurityContext(1L, MemberRole.MEMBER, null);
  }

  @Test
  @DisplayName("결제 계좌 입출금 내역 조회 성공")
  void getAccountLedgerPageSuccess() throws Exception {
    // given
    Long memberId = 1L;

    PaymentAccountLogDto dto =
        new PaymentAccountLogDto(
            1L,
            PaymentEventType.CHARGE_BANK_TRANSFER,
            BigDecimal.valueOf(5000),
            BigDecimal.valueOf(10000),
            LocalDateTime.of(2024, 1, 1, 12, 0));

    Page<PaymentAccountLogDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

    when(paymentFacade.getAccountLogPageListBySearch(
            eq(memberId), any(PaymentAccountSearchRequest.class)))
        .thenReturn(page);

    // when & then
    mockMvc
        .perform(get("/api/v1/payments/accounts/logs").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isSuccess").value(true))
        .andExpect(jsonPath("$.result[0].isDeposit").value(true))
        .andExpect(
            jsonPath("$.result[0].content")
                .value(PaymentEventType.CHARGE_BANK_TRANSFER.getDescription()))
        .andExpect(jsonPath("$.result[0].amount").value(5000))
        .andExpect(jsonPath("$.result[0].balance").value(10000))
        .andExpect(jsonPath("$.pageInfo.page").value(0))
        .andExpect(jsonPath("$.pageInfo.size").value(10))
        .andExpect(jsonPath("$.pageInfo.totalElements").value(1))
        .andExpect(jsonPath("$.pageInfo.totalPages").value(1));
  }

  @Test
  @DisplayName("from > to 이면 컨트롤러에서 5xx 에러 응답")
  void getAccountLedgerFailedFromAfterTo() throws Exception {
    // given
    String from = "2024-01-10T00:00:00";
    String to = "2024-01-01T00:00:00";

    // when & then
    mockMvc
        .perform(
            get("/api/v1/payments/accounts/logs")
                .param("page", "0")
                .param("size", "10")
                .param("from", from)
                .param("to", to))
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.isSuccess").value(false))
        .andExpect(jsonPath("$.code").exists())
        .andExpect(jsonPath("$.message").exists());
  }
}
