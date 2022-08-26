package com.managemc.spigot.service;

import com.managemc.api.ApiException;
import com.managemc.api.wrapper.ClientProvider;
import com.managemc.spigot.service.model.RemoteConfig;
import lombok.Getter;
import org.openapitools.client.model.PunishmentConfig;

public class RemoteConfigService {

  private final ClientProvider webClients;
  @Getter
  private RemoteConfig remoteConfig;

  public RemoteConfigService(ClientProvider webClients) {
    this.webClients = webClients;
  }

  public void loadSync() throws ApiException {
    PunishmentConfig punishmentConfig = webClients.externalServer()
        .getServerNetworksApi().fetchPunishmentConfig();
    this.remoteConfig = new RemoteConfig(punishmentConfig);
  }
}
