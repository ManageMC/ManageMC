package com.managemc.spigot.listener.handler;

import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.listener.base.util.EventLogicHandler;
import com.managemc.spigot.listener.handler.helper.PlayerMuteCheckable;
import com.managemc.spigot.state.model.MuteData;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncPlayerChatEventHandler extends PlayerMuteCheckable implements EventLogicHandler<AsyncPlayerChatEvent> {

  public AsyncPlayerChatEventHandler(SpigotPluginConfig config) {
    super(config.getMuteManager());
  }

  @Override
  public void handleEventLogic(AsyncPlayerChatEvent event) {
    MuteData muteData = fetchMuteDataAndHandleMute(event.getPlayer());
    if (muteData != null) {
      if (muteData.isShadow()) {
        event.getRecipients().clear();
        event.getRecipients().add(event.getPlayer());
      } else {
        event.setCancelled(true);
      }
    }
  }
}
