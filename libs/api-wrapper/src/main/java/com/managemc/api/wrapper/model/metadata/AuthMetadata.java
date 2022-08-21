package com.managemc.api.wrapper.model.metadata;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public abstract class AuthMetadata {
  @Getter
  private final long issuedAtMillis;
  @Getter
  private final long expiresAtMillis;

  public abstract AuthMetadataType getType();
}
