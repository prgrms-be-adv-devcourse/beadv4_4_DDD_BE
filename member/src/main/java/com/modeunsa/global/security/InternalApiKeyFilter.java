package com.modeunsa.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {
  private static final String INTERNAL_PATH = "/internal/";
  private static final String INTERNAL_HEADER = "X-INTERNAL-API-KEY";

  @Value("${internal.api-key}")
  private String expectedApiKey;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String uri = request.getRequestURI();

    if (!uri.contains(INTERNAL_PATH)) {
      filterChain.doFilter(request, response);
      return;
    }

    String apiKey = request.getHeader(INTERNAL_HEADER);

    if (!StringUtils.hasText(apiKey) || !expectedApiKey.equals(apiKey)) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Internal API Key");
      return;
    }

    filterChain.doFilter(request, response);
  }
}
