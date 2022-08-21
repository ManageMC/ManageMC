package com.managemc.api.wrapper.model.metadata;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class InternalAuthMetadata extends AuthMetadata {

  @Getter
  private final AuthMetadataType type = AuthMetadataType.INTERNAL;
}
