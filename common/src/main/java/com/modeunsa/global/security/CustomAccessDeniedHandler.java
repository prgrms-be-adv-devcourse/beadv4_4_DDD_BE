package com.modeunsa.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.ErrorStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** 인증은 되었지만 권한이 없는 경우 403 Forbidden 처리 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper =  new ObjectMapper();

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException, ServletException {

    log.warn("권한 부족 (403) - URI: {} - Message: {}", request.getRequestURI(), accessDeniedException.getMessage());

    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    ResponseEntity<ApiResponse> apiResponse = ApiResponse.onFailure(ErrorStatus.AUTH_ACCESS_DENIED);

    response.getWriter().write(objectMapper.writeValueAsString(apiResponse.getBody()));
  }
}