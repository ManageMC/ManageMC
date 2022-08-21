package com.managemc.api.wrapper.model.metadata;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class ExternalApplicationAuthMetadata extends AuthMetadata {

  @Getter
  private final AuthMetadataType type = AuthMetadataType.EXTERNAL;
  @Getter
  private final long serverNetworkId;
}
