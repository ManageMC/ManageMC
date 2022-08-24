package com.managemc.spigot.listener.handler;

import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.listener.base.util.EventLogicHandler;
import com.managemc.spigot.listener.handler.helper.SharedListenerLogic;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitEventHandler implements EventLogicHandler<PlayerQuitEvent> {

  private final SpigotPluginConfig config;

  public PlayerQuitEventHandler(SpigotPluginConfig config) {
    this.config = config;
  }

  @Override
  public void handleEventLogic(PlayerQuitEvent event) {
    SharedListenerLogic.onPlayerLeaveServer(config, event.getPlayer());
  }
}
