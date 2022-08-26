package com.managemc.account_linker.service;

import com.managemc.api.ApiException;
import com.managemc.api.wrapper.ClientProvider;

public class ApiPingService {

  private final ClientProvider webClients;

  public ApiPingService(ClientProvider webClients) {
    this.webClients = webClients;
  }

  public void ping() throws ApiException {
    webClients.internal().getPingApi().ping();
  }
}
