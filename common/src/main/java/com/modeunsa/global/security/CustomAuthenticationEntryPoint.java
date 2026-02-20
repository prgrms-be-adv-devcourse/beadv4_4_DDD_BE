package com.modeunsa.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modeunsa.global.status.ErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/** 인증되지 않은 사용자 (헤더 없음) 접근 시 401 Unauthorized 처리 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {

    String requestUri = request.getRequestURI();
    log.warn("인증되지 않은 접근 (401) - URI: {}", requestUri);

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    Map<String, Object> body = new HashMap<>();
    body.put("isSuccess", false);
    body.put("code", ErrorStatus.AUTH_UNAUTHORIZED.getCode());
    body.put("message", ErrorStatus.AUTH_UNAUTHORIZED.getMessage());

    objectMapper.writeValue(response.getOutputStream(), body);
  }
}
