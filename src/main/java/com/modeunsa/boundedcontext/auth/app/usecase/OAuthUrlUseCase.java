package com.modeunsa.boundedcontext.auth.app.usecase;

import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.auth.out.client.OAuthClientFactory;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.net.URI;
import java.net.URISyntaxException;
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

  /** OAuth2 로그인 URL 생성 */
  public String generateOAuthUrl(OAuthProvider provider, String redirectUri) {
    validateRedirectUri(redirectUri);
    return oauthClientFactory.getClient(provider).generateOAuthUrl(redirectUri);
  }

  private void validateRedirectUri(String redirectUri) {
    if (redirectUri == null) {
      return;
    }

    if (allowedRedirectDomains.isEmpty()) {
      throw new GeneralException(ErrorStatus.OAUTH_INVALID_REDIRECT_URI);
    }

    URI uri;
    try {
      uri = new URI(redirectUri);
    } catch (URISyntaxException e) {
      throw new GeneralException(ErrorStatus.OAUTH_INVALID_REDIRECT_URI);
    }

    String scheme = uri.getScheme();
    if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
      throw new GeneralException(ErrorStatus.OAUTH_INVALID_REDIRECT_URI);
    }

    String host = uri.getHost();
    if (host == null) {
      throw new GeneralException(ErrorStatus.OAUTH_INVALID_REDIRECT_URI);
    }

    boolean isAllowed =
        allowedRedirectDomains.stream()
            .anyMatch(
                allowedHost ->
                    host.equalsIgnoreCase(allowedHost)
                        || host.toLowerCase().endsWith("." + allowedHost.toLowerCase()));

    if (!isAllowed) {
      throw new GeneralException(ErrorStatus.OAUTH_INVALID_REDIRECT_URI);
    }
  }
}
