package com.managemc.api.wrapper.client;

import lombok.Getter;

public enum ApiHost {

  DEVELOPMENT(0),
  DEMO(1),
  PRODUCTION(2);

  @Getter
  private final int serverIndex;

  ApiHost(int serverIndex) {
    this.serverIndex = serverIndex;
  }
}
