package com.managemc.spigot.util.chat.formatter;

import com.managemc.spigot.util.chat.PluginMessageFormatter;
import lombok.NonNull;
import org.openapitools.client.model.PlayerWatchList;

public class WatchlistFormatter extends ChatFormatterBase<PlayerWatchList> {

  public static String EMPTY_LIST = "> Your watch list is empty";

  public WatchlistFormatter(PlayerWatchList obj) {
    super(obj);
  }

  @Override
  public @NonNull String format() {
    PluginMessageFormatter message = new PluginMessageFormatter().header("Personal Watch List:").indent();

    if (obj.getWatchedPlayers().isEmpty()) {
      message.newline().append(EMPTY_LIST);
    } else {
      obj.getWatchedPlayers().forEach(player -> {
        String printedPlayer = new WatchedPlayerFormatter(player).format();
        message.newline().append(printedPlayer);
      });
    }
    return message.toString();
  }
}
