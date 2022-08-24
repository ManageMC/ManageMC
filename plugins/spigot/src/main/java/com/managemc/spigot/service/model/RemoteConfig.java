package com.managemc.spigot.service.model;

import lombok.Getter;
import org.openapitools.client.model.PunishmentConfig;

import java.util.List;

public class RemoteConfig {

  @Getter
  private final List<String> blockedCommandsForMutedPlayers;

  public RemoteConfig(PunishmentConfig punishmentConfig) {
    this.blockedCommandsForMutedPlayers = punishmentConfig.getBlockedCommandsForMutedPlayers();
  }
}
