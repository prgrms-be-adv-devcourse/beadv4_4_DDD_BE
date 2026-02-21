package com.modeunsa.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modeunsa.global.config.InternalProperties;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.member.MemberRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@EnableConfigurationProperties(InternalProperties.class)
@RequiredArgsConstructor
public class GatewayHeaderFilter extends OncePerRequestFilter {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final InternalProperties internalProperties;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String userIdStr = request.getHeader("X-User-Id");
    String userRoleStr = request.getHeader("X-User-Role");
    String sellerIdStr = request.getHeader("X-Seller-Id");

    if (userIdStr != null && userRoleStr != null) {

      // 요청이 진짜 게이트웨이에서 온 것인지 비밀키로 검증
      String gatewayToken = request.getHeader("X-Gateway-Token");
      if (!internalProperties.getApiKey().equals(gatewayToken)) {
        logger.error("보안 경고: 유효하지 않은 게이트웨이 토큰으로 접근 시도");
        sendErrorResponse(response, ErrorStatus.UNAUTHORIZED, "유효하지 않은 내부 접근입니다.");
        return;
      }

      // 1. SYSTEM 계정 처리
      if ("SYSTEM".equals(userIdStr)) {
        CustomUserDetails systemUser = new CustomUserDetails(0L, MemberRole.SYSTEM, null);
        setAuthentication(systemUser);
      } else { // 2. 일반 유저 처리
        try {
          Long memberId = Long.parseLong(userIdStr);

          // API Gateway는 식별 문자열 그대로(예: "MEMBER") 전송하는 것을 표준으로 함.
          String roleName =
              userRoleStr.startsWith("ROLE_") ? userRoleStr.substring(5) : userRoleStr;
          MemberRole role = MemberRole.valueOf(roleName);

          Long sellerId = sellerIdStr != null ? Long.parseLong(sellerIdStr) : null;

          CustomUserDetails user = new CustomUserDetails(memberId, role, sellerId);
          setAuthentication(user);
        } catch (IllegalArgumentException e) {
          logger.error("Gateway 헤더 파싱 실패 (잘못된 데이터 형식): " + e.getMessage());
          sendErrorResponse(response, ErrorStatus.AUTH_UNAUTHORIZED, "인증 정보 형식이 올바르지 않습니다.");
          return;
        } catch (Exception e) {
          logger.error("Gateway 헤더 처리 중 예기치 않은 오류 발생: " + e.getMessage());
          sendErrorResponse(response, ErrorStatus.INTERNAL_SERVER_ERROR, "서버 내부 인증 처리 중 오류가 발생했습니다.");
          return;
        }
      }
    }

    filterChain.doFilter(request, response);
  }

  private void setAuthentication(CustomUserDetails userDetails) {
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private void sendErrorResponse(HttpServletResponse response, ErrorStatus errorStatus, String customMessage) throws IOException {
    response.setStatus(errorStatus.getHttpStatus().value());
    response.setContentType("application/json;charset=UTF-8");

    ApiResponse<Void> apiResponse = new ApiResponse<>(
        false,
        errorStatus.getCode(),
        customMessage != null ? customMessage : errorStatus.getMessage(),
        null,
        null
    );

    // ObjectMapper를 통해 객체를 JSON 문자열로 변환하여 응답
    String jsonResponse = objectMapper.writeValueAsString(apiResponse);
    response.getWriter().write(jsonResponse);
  }
}