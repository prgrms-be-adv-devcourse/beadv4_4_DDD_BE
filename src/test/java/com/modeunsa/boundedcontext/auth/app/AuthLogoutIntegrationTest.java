package com.modeunsa.boundedcontext.auth.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.modeunsa.boundedcontext.auth.domain.entity.AuthAccessTokenBlacklist;
import com.modeunsa.boundedcontext.auth.out.repository.AuthAccessTokenBlacklistRepository;
import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("로그아웃 통합 테스트")
class AuthLogoutIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private JwtTokenProvider jwtTokenProvider;

  @Autowired private AuthAccessTokenBlacklistRepository blacklistRepository;

  private String validAccessToken;
  private static final Long TEST_MEMBER_ID = 1L;

  @BeforeEach
  void setUp() {
    // 블랙리스트 초기화
    blacklistRepository.deleteAll();
    // 테스트용 토큰 생성 (실제 JwtTokenProvider 구현에 맞게 수정 필요)
    validAccessToken =
        jwtTokenProvider.createAccessToken(TEST_MEMBER_ID, MemberRole.valueOf("MEMBER"));
  }

  @Nested
  @DisplayName("블랙리스트 토큰 검증")
  class BlacklistedTokenValidation {

    @Test
    @DisplayName("블랙리스트에 등록된 토큰으로 API 요청 시 401 Unauthorized 반환")
    void requestWithBlacklistedTokenReturns401() throws Exception {
      // given - 토큰을 블랙리스트에 등록
      long remainingExpiration = jwtTokenProvider.getRemainingExpiration(validAccessToken);
      AuthAccessTokenBlacklist blacklist =
          AuthAccessTokenBlacklist.of(validAccessToken, TEST_MEMBER_ID, remainingExpiration);
      blacklistRepository.save(blacklist);

      // when & then - 블랙리스트된 토큰으로 요청
      mockMvc
          .perform(
              get("/api/v1/auths/me") // 인증 필요한 엔드포인트
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + validAccessToken))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("블랙리스트에 없는 유효한 토큰으로 API 요청 시 정상 처리")
    void requestWithValidTokenSuccess() throws Exception {
      // given - 블랙리스트에 등록하지 않음

      // when & then
      mockMvc
          .perform(
              get("/api/v1/auths/me")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + validAccessToken))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("로그아웃 API 통합 테스트")
  class LogoutApiIntegration {

    @Test
    @DisplayName("로그아웃 후 동일 토큰으로 재요청 시 401 반환")
    void afterLogoutSameTokenRequestReturns401() throws Exception {
      // given - 로그아웃 수행
      mockMvc
          .perform(
              post("/api/v1/auths/logout")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + validAccessToken))
          .andExpect(status().isOk());

      // when & then - 동일 토큰으로 재요청
      mockMvc
          .perform(
              get("/api/v1/auths/me")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + validAccessToken))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그아웃 API 정상 호출")
    void logoutSuccess() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/auths/logout")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + validAccessToken))
          .andExpect(status().isOk());
    }
  }
}
