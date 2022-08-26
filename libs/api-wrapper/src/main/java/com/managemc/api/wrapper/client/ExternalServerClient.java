package com.managemc.api.wrapper.client;

import com.managemc.api.wrapper.model.metadata.ExternalServerAuthMetadata;
import com.managemc.api.wrapper.refresher.TokenRefresher;

public class ExternalServerClient extends Client<ExternalServerAuthMetadata> {

  public ExternalServerClient(TokenRefresher<ExternalServerAuthMetadata> refresher) {
    super(refresher);
  }
}
