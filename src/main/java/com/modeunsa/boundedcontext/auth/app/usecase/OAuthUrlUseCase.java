package com.modeunsa.boundedcontext.auth.app.usecase;

import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.auth.out.client.OAuthClientFactory;
import com.modeunsa.global.config.SecurityProperties;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthUrlUseCase {
  private final OAuthClientFactory oauthClientFactory;
  private final SecurityProperties securityProperties;

  /** OAuth2 로그인 URL 생성 */
  public String generateOAuthUrl(OAuthProvider provider, String redirectUri) {
    validateRedirectUri(redirectUri);
    return oauthClientFactory.getClient(provider).generateOAuthUrl(redirectUri);
  }

  private void validateRedirectUri(String redirectUri) {
    if (redirectUri == null) {
      return;
    }

    List<String> allowedRedirectDomains = securityProperties.getOauth2().getAllowedRedirectDomains();

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
