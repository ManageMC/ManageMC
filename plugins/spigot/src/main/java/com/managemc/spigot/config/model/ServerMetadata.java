package com.managemc.spigot.config.model;

import lombok.Builder;
import lombok.Getter;

@Builder
public class ServerMetadata {
  @Getter
  private final long serverId;
  @Getter
  private final long serverGroupId;
  @Getter
  private final String serverGroup;
}
