package com.managemc.spigot.listener.handler;

import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.listener.base.util.EventLogicHandler;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerLoginEventHandler implements EventLogicHandler<PlayerLoginEvent> {

  private final SpigotPluginConfig config;

  public PlayerLoginEventHandler(SpigotPluginConfig config) {
    this.config = config;
  }

  @Override
  public void handleEventLogic(PlayerLoginEvent event) {
    config.getPermissionsManager().assignPermissions(event.getPlayer());
  }
}
