package com.managemc.spigot.util.comparator;

import com.managemc.spigot.util.oneof.PlayerPunishmentType;
import org.openapitools.client.model.PlayerPunishment;

import java.util.Comparator;

public class PlayerPunishmentComparator implements Comparator<PlayerPunishment> {

  @Override
  public int compare(PlayerPunishment p1, PlayerPunishment p2) {
    return (int) (issuedAt(p2) - issuedAt(p1));
  }

  private Long issuedAt(PlayerPunishment punishment) {
    PlayerPunishmentType type = PlayerPunishmentType.of(punishment);
    switch (type) {
      case BAN_OR_MUTE:
        return punishment.getBanOrMute().getIssuedAt();
      case WARNING:
        return punishment.getWarning().getIssuedAt();
      default:
        throw new RuntimeException("Unhandled enum value " + type);
    }
  }
}
