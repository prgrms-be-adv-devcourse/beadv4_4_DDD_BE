package com.modeunsa.boundedcontext.auth.app.usecase;

import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.auth.out.client.OAuthClientFactory;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthUrlUseCase {
  private final OAuthClientFactory oauthClientFactory;

  private static final List<String> ALLOWED_REDIRECT_DOMAINS = List.of(
      "https://modeunsa.com",
      "https://www.modeunsa.com",
      "http://localhost:3000",
      "http://localhost:8080"
  );

  /**
   * OAuth2 로그인 URL 생성
   */
  public String generateOAuthUrl(OAuthProvider provider, String redirectUri) {
    validateRedirectUri(redirectUri);
    return oauthClientFactory.getClient(provider).generateOAuthUrl(redirectUri);
  }

  private void validateRedirectUri(String redirectUri) {
    if (redirectUri == null) {
      return;  // null이면 기본값 사용하니까 OK
    }

    boolean isAllowed = ALLOWED_REDIRECT_DOMAINS.stream()
        .anyMatch(redirectUri::startsWith);

    if (!isAllowed) {
      throw new GeneralException(ErrorStatus.OAUTH_INVALID_REDIRECT_URI);
    }
  }
}
