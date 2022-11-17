package com.managemc.api.wrapper.client;

import lombok.Getter;

public enum ApiHost {

  DEVELOPMENT(0, "http://localhost:9070/api/v1"),
  DEMO(1, "https://api-demo.managemc.com/api/v1"),
  PRODUCTION(2, "https://api.managemc.com/api/v1");

  @Getter
  private final int serverIndex;
  @Getter
  private final String baseUrl;

  ApiHost(int serverIndex, String baseUrl) {
    this.serverIndex = serverIndex;
    this.baseUrl = baseUrl;
  }
}
