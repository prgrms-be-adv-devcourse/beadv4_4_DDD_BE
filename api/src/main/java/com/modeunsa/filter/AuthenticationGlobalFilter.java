package com.modeunsa.filter;

import com.modeunsa.client.AuthServiceClient;
import com.modeunsa.config.GatewaySecurityProperties;
import com.modeunsa.config.InternalProperties;
import com.modeunsa.dto.AuthStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationGlobalFilter implements GlobalFilter, Ordered {

  private final AuthServiceClient authServiceClient;
  private final InternalProperties internalProperties;
  private final GatewaySecurityProperties securityProperties;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    String path = request.getPath().value();

    // 1. /api/로 시작하지 않으면 통과
    if (!path.startsWith("/api/")) {
      return chain.filter(exchange);
    }

    // 2. 공개 엔드포인트 체크
    if (isPublicEndpoint(path)) {
      return chain.filter(exchange);
    }

    // 3. Internal API Key 체크
    String internalApiKeyHeader = request.getHeaders().getFirst("X-INTERNAL-API-KEY");
    if (internalApiKeyHeader != null
        && internalApiKeyHeader.equals(internalProperties.getApiKey())) {
      return addInternalUserHeaders(exchange, chain);
    }

    // 4. 일반 유저 토큰(JWT) 체크 (헤더와 쿠키 모두 확인)
    String accessToken = null;
    String authHeader = request.getHeaders().getFirst("Authorization");

    // 1) 먼저 Authorization 헤더에서 토큰을 찾아본다
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      accessToken = authHeader.substring(7).trim();
    }
    // 2) 헤더에 없다면, accessToken 이름의 쿠키가 있는지 찾아본다
    else if (request.getCookies().containsKey("accessToken")) {
      accessToken = request.getCookies().getFirst("accessToken").getValue();
    }

    // 3) 헤더와 쿠키 둘 다 뒤져봤는데도 토큰이 없으면 401 에러
    if (accessToken == null || accessToken.isBlank()) {
      return unauthorized(exchange);
    }

    // 5. Member 서비스로 토큰 검증
    return authServiceClient
        .validateToken(accessToken)
        .flatMap(
            authStatus -> {
              if (!authStatus.isAuthenticated()) {
                return unauthorized(exchange);
              }
              return addUserHeaders(exchange, chain, authStatus);
            })
        .onErrorResume(e -> unauthorized(exchange));
  }

  private boolean isPublicEndpoint(String path) {
    return securityProperties.getPermitUrls().stream()
        .anyMatch(pattern -> pathMatcher.match(pattern, path));
  }

  private Mono<Void> addUserHeaders(
      ServerWebExchange exchange, GatewayFilterChain chain, AuthStatusResponse authStatus) {

    ServerHttpRequest.Builder requestBuilder =
        exchange
            .getRequest()
            .mutate()
            .header("X-User-Id", authStatus.getMemberId())
            .header("X-User-Role", authStatus.getRole()); // 예: MEMBER, SELLER

    // sellerId가 null이 아닌 경우(판매자인 경우)에만 헤더에 추가
    if (authStatus.getSellerId() != null) {
      requestBuilder.header("X-Seller-Id", String.valueOf(authStatus.getSellerId()));
    }

    return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
  }

  private Mono<Void> addInternalUserHeaders(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest mutatedRequest =
        exchange
            .getRequest()
            .mutate()
            .header("X-User-Id", "SYSTEM")
            .header("X-User-Role", "ROLE_SYSTEM")
            .build();

    return chain.filter(exchange.mutate().request(mutatedRequest).build());
  }

  private Mono<Void> unauthorized(ServerWebExchange exchange) {
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    return exchange.getResponse().setComplete();
  }

  @Override
  public int getOrder() {
    return -1;
  }
}
