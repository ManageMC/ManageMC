package com.managemc.spigot.util.oneof;

import lombok.Getter;
import org.openapitools.client.model.PlayerPunishment;

public enum PlayerPunishmentType {
  BAN_OR_MUTE("BanOrMute"), WARNING("Warning");

  @Getter
  private final String className;

  PlayerPunishmentType(String className) {
    this.className = className;
  }

  public static PlayerPunishmentType of(PlayerPunishment oneOf) {
    String oneOfClassName = oneOf.getActualInstance().getClass().getSimpleName();
    for (PlayerPunishmentType type : values()) {
      if (type.getClassName().equals(oneOfClassName)) {
        return type;
      }
    }
    throw new RuntimeException("Unexpected PlayerPunishment (oneOf) schema: " + oneOfClassName);
  }
}
