package com.managemc.spigot.util.chat.formatter;

import com.managemc.spigot.util.chat.PluginMessageFormatter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.openapitools.client.model.PlayerActiveServer;
import org.openapitools.client.model.WatchedPlayer;

public class WatchedPlayerFormatter extends ChatFormatterBase<WatchedPlayer> {

  public WatchedPlayerFormatter(WatchedPlayer obj) {
    super(obj);
  }

  @Override
  public @NonNull String format() {
    PlayerActiveServer server = obj.getActiveServer();

    return new PluginMessageFormatter()
        .append("- ")
        .append(server == null ? obj.getUsername() : onlineMessage(server))
        .append(ChatColor.RESET.toString())
        .toString();
  }

  private String onlineMessage(PlayerActiveServer server) {
    return String.format(
        "%s%s%s:%s%s %s (%s)",
        ChatColor.GREEN,
        ChatColor.BOLD,
        obj.getUsername(),
        ChatColor.RESET,
        ChatColor.GREEN,
        server.getName(),
        server.getGroup()
    );
  }
}
