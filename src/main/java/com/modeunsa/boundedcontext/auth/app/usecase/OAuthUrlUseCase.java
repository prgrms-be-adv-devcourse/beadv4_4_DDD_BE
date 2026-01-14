package com.modeunsa.boundedcontext.auth.app.usecase;

import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.auth.out.client.OAuthClientFactory;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthUrlUseCase {
  private final OAuthClientFactory oauthClientFactory;

  @Value("${security.oauth2.allowed-redirect-domains:}")
  private List<String> allowedRedirectDomains;

  /**
   * OAuth2 로그인 URL 생성
   */
  public String generateOAuthUrl(OAuthProvider provider, String redirectUri) {
    validateRedirectUri(redirectUri);
    return oauthClientFactory.getClient(provider).generateOAuthUrl(redirectUri);
  }

  private void validateRedirectUri(String redirectUri) {
    if (redirectUri == null) {
      return; // null이면 기본 redirect-uri 사용
    }

    if (allowedRedirectDomains.isEmpty()) {
      throw new GeneralException(ErrorStatus.OAUTH_INVALID_REDIRECT_URI);
    }

    boolean isAllowed = allowedRedirectDomains.stream()
        .anyMatch(redirectUri::startsWith);

    if (!isAllowed) {
      throw new GeneralException(ErrorStatus.OAUTH_INVALID_REDIRECT_URI);
    }
  }
}
