package com.managemc.api.wrapper.model;

import lombok.Getter;

public class Keys {
  @Getter
  private final String publicKey;
  @Getter
  private final String privateKey;

  public Keys(String publicKey, String privateKey) {
    this.publicKey = publicKey;
    this.privateKey = privateKey;
  }
}
