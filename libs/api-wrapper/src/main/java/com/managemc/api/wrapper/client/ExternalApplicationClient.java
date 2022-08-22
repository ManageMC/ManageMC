package com.managemc.api.wrapper.client;

import com.managemc.api.wrapper.model.metadata.ExternalApplicationAuthMetadata;
import com.managemc.api.wrapper.refresher.TokenRefresher;

public class ExternalApplicationClient extends Client<ExternalApplicationAuthMetadata> {

  public ExternalApplicationClient(TokenRefresher<ExternalApplicationAuthMetadata> refresher) {
    super(refresher);
  }
}
