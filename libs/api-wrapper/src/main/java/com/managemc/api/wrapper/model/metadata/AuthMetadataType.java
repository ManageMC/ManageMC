package com.managemc.api.wrapper.model.metadata;

import lombok.Getter;

/**
 * Hard-coded constants of great significance. Do not change!
 */
public enum AuthMetadataType {
  PLAYER("user_minecraft"),
  EXTERNAL("external_minecraft"),
  INTERNAL("internal_minecraft"),
  ;

  @Getter
  private final String type;

  AuthMetadataType(String type) {
    this.type = type;
  }
}
