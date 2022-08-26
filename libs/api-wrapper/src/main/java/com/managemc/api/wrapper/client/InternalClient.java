package com.managemc.api.wrapper.client;

import com.managemc.api.wrapper.model.metadata.InternalAuthMetadata;
import com.managemc.api.wrapper.refresher.TokenRefresher;

public class InternalClient extends Client<InternalAuthMetadata> {
  public InternalClient(TokenRefresher<InternalAuthMetadata> refresher) {
    super(refresher);
  }
}
