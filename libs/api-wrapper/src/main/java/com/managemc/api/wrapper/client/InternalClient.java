package com.managemc.api.wrapper.client;

import com.managemc.api.wrapper.model.metadata.InternalAuthMetadata;
import com.managemc.api.wrapper.refresher.WebServiceTokenRefresher;

public class InternalClient extends Client<InternalAuthMetadata> {
  public InternalClient(WebServiceTokenRefresher<InternalAuthMetadata> refresher) {
    super(refresher);
  }
}
