package com.managemc.api.wrapper.refresher;

import com.managemc.api.ApiException;
import com.managemc.api.auth.HttpBearerAuth;
import com.managemc.api.wrapper.ClientProvider;
import com.managemc.api.wrapper.model.metadata.AuthMetadataType;
import com.managemc.api.wrapper.model.metadata.PlayerAuthMetadata;
import org.openapitools.client.model.GeneratePlayerTokenInput;

import java.util.Map;
import java.util.UUID;

public class PlayerTokenRefresher extends WebServiceTokenRefresher<PlayerAuthMetadata> {

  private final UUID playerId;
  private final ClientProvider provider;

  PlayerTokenRefresher(ClientProvider.Logger logger, String basePath, UUID playerId, ClientProvider provider) {
    super(logger, basePath);

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
    Map<String, Object> authMetadataMap = tokenToJsonMap(token);

    @SuppressWarnings("unchecked")
    Map<String, Object> playerData = (Map<String, Object>) authMetadataMap.get("player");

    PlayerAuthMetadata.Player player = PlayerAuthMetadata.Player.builder()
        .id(Long.parseLong(playerData.get("id").toString()))
        .username(playerData.get("username").toString())
        .uuid(UUID.fromString(playerData.get("uuid").toString()))
        .build();

    this.authMetadata = PlayerAuthMetadata.builder()
        .issuedAtMillis(Long.parseLong(authMetadataMap.get("iat").toString()) * 1000)
        .expiresAtMillis(Long.parseLong(authMetadataMap.get("exp").toString()) * 1000)
        .serverNetworkId(Long.parseLong(authMetadataMap.get("server_network_id").toString()))
        .serverGroupId(Long.parseLong(authMetadataMap.get("server_group_id").toString()))
        .player(player)
        .build();
  }
}
