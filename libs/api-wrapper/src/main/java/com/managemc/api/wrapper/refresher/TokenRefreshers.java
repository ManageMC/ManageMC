package com.managemc.api.wrapper.refresher;

import com.managemc.api.wrapper.ClientProvider;
import com.managemc.api.wrapper.client.ApiHost;
import com.managemc.api.wrapper.model.Keys;

import java.util.UUID;

public class TokenRefreshers {

  private TokenRefreshers() {
  }

  public static InternalTokenRefresher internal(
      ClientProvider.Logger logger,
      ApiHost apiHost,
      Keys keys
  ) {
    return new InternalTokenRefresher(logger, apiHost, keys);
  }

  public static ExternalServerTokenRefresher externalServer(
      ClientProvider.Logger logger,
      ApiHost apiHost,
      Keys keys,
      String serverGroup
  ) {
    return new ExternalServerTokenRefresher(logger, apiHost, keys, serverGroup);
  }

  public static ExternalApplicationTokenRefresher externalApplication(
      ClientProvider.Logger logger,
      ApiHost apiHost,
      Keys keys
  ) {
    return new ExternalApplicationTokenRefresher(logger, apiHost, keys);
  }

  public static PlayerTokenRefresher player(
      ClientProvider.Logger logger,
      ApiHost apiHost,
      UUID playerId,
      ClientProvider provider
  ) {
    return new PlayerTokenRefresher(logger, apiHost, playerId, provider);
  }
}
