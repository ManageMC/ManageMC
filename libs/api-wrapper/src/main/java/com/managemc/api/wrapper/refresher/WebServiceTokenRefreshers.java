package com.managemc.api.wrapper.refresher;

import com.managemc.api.wrapper.ClientProvider;
import com.managemc.api.wrapper.model.Keys;

import java.util.UUID;

public class WebServiceTokenRefreshers {

  private WebServiceTokenRefreshers() {
  }

  public static InternalTokenRefresher internal(
      ClientProvider.Logger logger,
      String basePath,
      Keys keys
  ) {
    return new InternalTokenRefresher(logger, basePath, keys);
  }

  public static ExternalServerTokenRefresher externalServer(
      ClientProvider.Logger logger,
      String basePath,
      Keys keys,
      String serverGroup
  ) {
    return new ExternalServerTokenRefresher(logger, basePath, keys, serverGroup);
  }

  public static ExternalApplicationTokenRefresher externalApplication(
      ClientProvider.Logger logger,
      String basePath,
      Keys keys
  ) {
    return new ExternalApplicationTokenRefresher(logger, basePath, keys);
  }

  public static PlayerTokenRefresher player(
      ClientProvider.Logger logger,
      String basePath,
      UUID playerId,
      ClientProvider provider
  ) {
    return new PlayerTokenRefresher(logger, basePath, playerId, provider);
  }
}
