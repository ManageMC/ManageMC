package com.managemc.api.wrapper.client;

import com.managemc.api.wrapper.model.metadata.ExternalServerAuthMetadata;
import com.managemc.api.wrapper.refresher.WebServiceTokenRefresher;

public class ExternalServerClient extends Client<ExternalServerAuthMetadata> {

  public ExternalServerClient(WebServiceTokenRefresher<ExternalServerAuthMetadata> refresher) {
    super(refresher);
  }
}
