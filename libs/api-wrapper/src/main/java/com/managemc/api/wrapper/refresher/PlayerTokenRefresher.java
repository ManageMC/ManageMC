package com.managemc.api.wrapper.refresher;

import com.managemc.api.ApiException;
import com.managemc.api.auth.HttpBearerAuth;
import com.managemc.api.wrapper.ClientProvider;
import com.managemc.api.wrapper.client.ApiHost;
import com.managemc.api.wrapper.model.metadata.AuthMetadataType;
import com.managemc.api.wrapper.model.metadata.PlayerAuthMetadata;
import org.openapitools.client.model.GeneratePlayerTokenInput;

import java.util.UUID;

public class PlayerTokenRefresher extends TokenRefresher<PlayerAuthMetadata> {

  private final UUID playerId;
  private final ClientProvider provider;

  PlayerTokenRefresher(ClientProvider.Logger logger, ApiHost apiHost, UUID playerId, ClientProvider provider) {
    super(logger, apiHost);

    this.playerId = playerId;
    this.provider = provider;
  }

  @Override
  void refreshToken() throws ApiException {
    String token = fetchNewToken();
    setToken(token);
    setAuthMetadataFromToken(token);
  }

  private String fetchNewToken() throws ApiException {
    GeneratePlayerTokenInput input = new GeneratePlayerTokenInput()
        .playerUuid(playerId);
    // token is guaranteed to be non-null in this case (if there is problem, API will indicate non-200 status)
    return provider.externalServer().getAuthenticationApi().generatePlayerToken(input).getToken();
  }

  private void setToken(String token) {
    HttpBearerAuth auth = (HttpBearerAuth) client
        .getAuthentication(AuthMetadataType.PLAYER.getType());
    auth.setBearerToken(token);
  }

  private void setAuthMetadataFromToken(String token) {
    this.authMetadata = tokenToSerializedObject(token, PlayerAuthMetadata.class);
  }
}
