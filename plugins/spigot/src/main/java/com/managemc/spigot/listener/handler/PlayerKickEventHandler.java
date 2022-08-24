package com.managemc.spigot.listener.handler;

import com.managemc.api.ApiException;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.listener.base.util.EventLogicHandler;
import com.managemc.spigot.listener.handler.helper.SharedListenerLogic;
import org.bukkit.event.player.PlayerKickEvent;
import org.openapitools.client.model.PlayerNote;

public class PlayerKickEventHandler implements EventLogicHandler<PlayerKickEvent> {

  static final String LOG_MESSAGE = "Made a note on %s's profile about the kick";
  static final String WARNING_MESSAGE = "%s is a real player, but does not exist in our database yet, so we were unable to make a note on their profile.";

  private final SpigotPluginConfig config;

  public PlayerKickEventHandler(SpigotPluginConfig config) {
    this.config = config;
  }

  @Override
  public void handleEventLogic(PlayerKickEvent event) {
    SharedListenerLogic.onPlayerLeaveServer(config, event.getPlayer());

    String username = event.getPlayer().getName();

    new Thread(() -> {
      try {
        PlayerNote note = config.getNoteService().createNoteForKickedPlayer(event);
        if (note == null) {
          config.getLogging().logWarning(String.format(WARNING_MESSAGE, username));
        } else {
          config.getLogging().logInfo(String.format(LOG_MESSAGE, username));
        }
      } catch (ApiException e) {
        config.getLogging().logStackTrace(e);
      }
    }).start();
  }
}
