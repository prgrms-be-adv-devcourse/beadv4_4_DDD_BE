package com.modeunsa.boundedcontext.payment.in.v1;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberDto;
import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberResponse;
import com.modeunsa.boundedcontext.payment.app.mapper.PaymentMapper;
import com.modeunsa.boundedcontext.payment.in.BasePaymentControllerTest;
import com.modeunsa.boundedcontext.payment.in.api.v1.PaymentMemberController;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

@DisplayName("PaymentMemberController 테스트")
class PaymentMemberControllerTest extends BasePaymentControllerTest {

  @Mock private PaymentFacade paymentFacade;
  @Mock private PaymentMapper paymentMapper;

  @BeforeEach
  void setUp() {
    super.setUpBase();
    setUpMockMvc(new PaymentMemberController(paymentFacade, paymentMapper));
    setSecurityContext(1L, MemberRole.MEMBER, null);
  }

  @Test
  @DisplayName("회원 정보 조회 성공 - 유효한 요청으로 계좌 정보 조회 성공")
  void getAccountBalanceSuccess() throws Exception {
    // given
    Long memberId = 1L;
    String customerKey = "customer_key_123";
    String customerName = "홍길동";
    String customerEmail = "test@example.com";
    BigDecimal balance = BigDecimal.valueOf(50000.00);

    PaymentMemberDto dto = new PaymentMemberDto(customerKey, customerEmail, customerName, balance);

    PaymentMemberResponse response =
        new PaymentMemberResponse(customerKey, customerEmail, customerName, balance);

    when(paymentFacade.getMember(memberId)).thenReturn(dto);
    when(paymentMapper.toPaymentMemberResponse(dto)).thenReturn(response);

    // when, then
    mockMvc
        .perform(get("/api/v1/payments/members"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isSuccess").value(true))
        .andExpect(jsonPath("$.result.customerKey").value(customerKey))
        .andExpect(jsonPath("$.result.customerName").value(customerName))
        .andExpect(jsonPath("$.result.customerEmail").value(customerEmail))
        .andExpect(jsonPath("$.result.balance").value(50000));
  }

  @Test
  @DisplayName("회원 정보 조회 실패 - 계좌를 찾을 수 없는 경우")
  void getMemberFailureAccountNotFound() throws Exception {
    // given
    Long memberId = 1L;

    when(paymentFacade.getMember(memberId))
        .thenThrow(new GeneralException(ErrorStatus.PAYMENT_ACCOUNT_NOT_FOUND));

    // when, then
    mockMvc
        .perform(get("/api/v1/payments/members"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.isSuccess").value(false))
        .andExpect(jsonPath("$.code").exists())
        .andExpect(jsonPath("$.message").exists());
  }
}
