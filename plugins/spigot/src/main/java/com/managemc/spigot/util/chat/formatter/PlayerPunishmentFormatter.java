package com.managemc.spigot.util.chat.formatter;

import com.managemc.spigot.state.ServerGroupsData;
import com.managemc.spigot.util.oneof.PlayerPunishmentType;
import lombok.NonNull;
import org.openapitools.client.model.PlayerPunishment;

public class PlayerPunishmentFormatter extends ChatFormatterBase<PlayerPunishment> {

  private final ServerGroupsData serverGroups;

  public PlayerPunishmentFormatter(PlayerPunishment obj, ServerGroupsData serverGroups) {
    super(obj);
    this.serverGroups = serverGroups;
  }

  @Override
  @NonNull
  public String format() {
    switch (PlayerPunishmentType.of(obj)) {
      case BAN_OR_MUTE:
        return new BanOrMuteFormatter(obj.getBanOrMute(), serverGroups).format();
      case WARNING:
        return new WarningFormatter(obj.getWarning()).format();
      default:
        throw new RuntimeException("Unexpected PlayerPunishmentType " + PlayerPunishmentType.of(obj));
    }
  }
}
