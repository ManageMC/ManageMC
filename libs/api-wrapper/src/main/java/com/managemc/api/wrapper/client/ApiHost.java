package com.managemc.api.wrapper.client;

import lombok.Getter;

public enum ApiHost {

  DEVELOPMENT(0, "http://localhost:9070/api/v1", "demo-api.crt"),
  DEMO(1, "https://api-demo.managemc.com/api/v1", "demo-api.crt"),
  PRODUCTION(2, "https://api.managemc.com/api/v1", "api.crt");

  @Getter
  private final int serverIndex;
  @Getter
  private final String baseUrl;
  @Getter
  private final String bundledCert;

  ApiHost(int serverIndex, String baseUrl, String bundledCert) {
    this.serverIndex = serverIndex;
    this.baseUrl = baseUrl;
    this.bundledCert = bundledCert;
  }
}
