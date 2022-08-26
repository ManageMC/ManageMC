package com.managemc.api.wrapper.client;

import com.managemc.api.wrapper.model.metadata.PlayerAuthMetadata;
import com.managemc.api.wrapper.refresher.TokenRefresher;

public class PlayerClient extends Client<PlayerAuthMetadata> {

  public PlayerClient(TokenRefresher<PlayerAuthMetadata> refresher) {
    super(refresher);
  }
}
