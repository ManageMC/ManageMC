package com.managemc.spigot.service;

import com.managemc.api.ApiException;
import com.managemc.spigot.config.SpigotPluginConfig;

public class ApiPingService {

  static final String UNAVAILABLE_MESSAGE = "ManageMC appears to be unavailable.";
  static final String UNHANDLED_ERROR_MESSAGE = "Unhandled runtime exception upon making an API call to ManageMC: internal service returned a status of %s";

  private final SpigotPluginConfig config;

  public ApiPingService(SpigotPluginConfig config) {
    this.config = config;
  }

  public void ping() {
    try {
      config.getClientProvider().externalServer().getPingApi().ping();
    } catch (ApiException e) {
      if (e.getCode() >= 500) {
        errorOut(UNAVAILABLE_MESSAGE);
      }

      errorOut(String.format(UNHANDLED_ERROR_MESSAGE, e.getCode()));
    }
  }

  private void errorOut(String message) {
    throw new RuntimeException(message);
  }
}
