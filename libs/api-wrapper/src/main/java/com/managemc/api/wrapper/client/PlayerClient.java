package com.managemc.api.wrapper.client;

import com.managemc.api.wrapper.model.metadata.PlayerAuthMetadata;
import com.managemc.api.wrapper.refresher.WebServiceTokenRefresher;

public class PlayerClient extends Client<PlayerAuthMetadata> {

  public PlayerClient(WebServiceTokenRefresher<PlayerAuthMetadata> refresher) {
    super(refresher);
  }
}
