package com.managemc.api.wrapper.model.metadata;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class ExternalServerAuthMetadata extends AuthMetadata {

  @Getter
  private final AuthMetadataType type = AuthMetadataType.EXTERNAL;

  @Getter
  @SerializedName(value = "server_network_id")
  private final long serverNetworkId;

  @Getter
  @SerializedName(value = "server_group_id")
  private final long serverGroupId;
}
