package com.managemc.api.wrapper.model.metadata;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@SuperBuilder
public class PlayerAuthMetadata extends AuthMetadata {

  @Getter
  private final AuthMetadataType type = AuthMetadataType.PLAYER;
  @Getter
  @SerializedName(value = "server_network_id")
  private final long serverNetworkId;
  @Getter
  @SerializedName(value = "server_group_id")
  private final long serverGroupId;
  @Getter
  @NonNull
  private final Player player;

  @Builder
  public static class Player {
    @Getter
    private final long id;
    @Getter
    private final String username;
    @Getter
    private final UUID uuid;
  }
}
