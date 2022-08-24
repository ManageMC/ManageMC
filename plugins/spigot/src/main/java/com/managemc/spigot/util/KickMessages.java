package com.managemc.spigot.util;

import org.bukkit.ChatColor;
import org.openapitools.client.model.*;

import java.util.Optional;

public class KickMessages {

  public static final String BAN_MESSAGE = "You have been banned from this server.";
  public static final String SHADOW_BAN_MESSAGE = "Server closed";
  public static final String WARNING_MESSAGE = "You have received a formal warning on this server.";

  public static String banMessage(BanOrMute ban) {
    return banOrIpBanMessage(ban.getReason(), ban.getIssuedAt(), ban.getDuration(), ban.getVisibility());
  }

  public static String banMessage(CreateBanOrMuteInput ban) {
    return banOrIpBanMessage(ban.getReason(), System.currentTimeMillis(), ban.getDurationMillis(), ban.getVisibility());
  }

  public static String ipBanMessage(IpBan ban) {
    return banOrIpBanMessage(ban.getReason(), ban.getIssuedAt(), ban.getDuration(), ban.getVisibility());
  }

  public static String ipBanMessage(CreateIpBanInput ban) {
    return banOrIpBanMessage(ban.getReason(), System.currentTimeMillis(), ban.getDurationMillis(), ban.getVisibility());
  }

  public static String warningMessage(Warning warning) {
    return warningMessage(warning.getReason());
  }

  public static String warningMessage(String reason) {
    return ChatColor.YELLOW + WARNING_MESSAGE + ChatColor.RESET +
        "\n\n" + ChatColor.YELLOW + "reason: " + Optional.ofNullable(reason).orElse("") + ChatColor.RESET +
        "\n\n" + ChatColor.YELLOW + "You must acknowledge the warning on ManageMC to resume playing: https://managemc.com/warnings" + ChatColor.RESET;
  }

  public static String banOrIpBanMessage(String reason, long issuedAt, Long duration, PunishmentVisibility visibility) {
    if (visibility == PunishmentVisibility.SHADOW) {
      return SHADOW_BAN_MESSAGE;
    }

    Long expiresAtMillis = duration == null ? null : issuedAt + duration;
    String formattedExpiresAt = TimeHelper.formatTimestamp(expiresAtMillis, "never");
    String relativeExpiresAt = expiresAtMillis == null ? "" : " (" + TimeHelper.relativeTime(expiresAtMillis) + ")";

    return ChatColor.YELLOW + BAN_MESSAGE + ChatColor.RESET +
        "\n\n" + ChatColor.YELLOW + "reason: " + Optional.ofNullable(reason).orElse("") + ChatColor.RESET +
        "\n" + ChatColor.YELLOW + "expires: " + formattedExpiresAt + relativeExpiresAt + ChatColor.RESET;
  }
}
