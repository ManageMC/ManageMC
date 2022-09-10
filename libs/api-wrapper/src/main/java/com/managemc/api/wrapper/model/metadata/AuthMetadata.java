package com.managemc.api.wrapper.model.metadata;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public abstract class AuthMetadata {
  @Getter
  private final long iat;
  @Getter
  private final long exp;

  public abstract AuthMetadataType getType();
}
