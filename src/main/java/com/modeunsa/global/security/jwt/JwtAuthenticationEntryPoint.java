package com.modeunsa.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modeunsa.global.exception.GeneralException;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {

    // Filter에서 저장한 예외 확인
    GeneralException exception = (GeneralException) request.getAttribute("exception");

    ErrorStatus errorStatus;
    if (exception != null) {
      errorStatus = exception.getErrorStatus();
    } else {
      errorStatus = ErrorStatus.AUTH_INVALID_TOKEN;
    }

    log.warn("인증 실패 - URI: {}, Code: {}", request.getRequestURI(), errorStatus.getCode());

    response.setStatus(errorStatus.getHttpStatus().value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    Map<String, Object> body = new HashMap<>();
    body.put("isSuccess", false);
    body.put("code", errorStatus.getCode());
    body.put("message", errorStatus.getMessage());

    objectMapper.writeValue(response.getOutputStream(), body);
  }
}
