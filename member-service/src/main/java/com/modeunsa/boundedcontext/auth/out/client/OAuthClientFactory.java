package com.modeunsa.boundedcontext.auth.out.client;

import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class OAuthClientFactory {

  private final Map<OAuthProvider, OAuthClient> clients;

  public OAuthClientFactory(List<OAuthClient> oauthClients) {
    this.clients =
        oauthClients.stream()
            .collect(
                Collectors.toMap(
                    OAuthClient::getProvider, // key: 각 클라이언트의 getProvider() 결과
                    Function.identity() // value: 클라이언트 객체 자체
                    ));
  }

  public OAuthClient getClient(OAuthProvider provider) {
    OAuthClient client = clients.get(provider);
    if (client == null) {
      throw new GeneralException(ErrorStatus.OAUTH_INVALID_PROVIDER);
    }
    return client;
  }
}
