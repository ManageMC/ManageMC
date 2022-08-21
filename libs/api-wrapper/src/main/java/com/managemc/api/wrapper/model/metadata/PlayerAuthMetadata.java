package com.managemc.api.wrapper.model.metadata;

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
  private final long serverNetworkId;
  @Getter
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
