package com.managemc.api.wrapper.client;

import com.managemc.api.wrapper.model.metadata.ExternalApplicationAuthMetadata;
import com.managemc.api.wrapper.refresher.WebServiceTokenRefresher;

public class ExternalApplicationClient extends Client<ExternalApplicationAuthMetadata> {

  public ExternalApplicationClient(WebServiceTokenRefresher<ExternalApplicationAuthMetadata> refresher) {
    super(refresher);
  }
}
