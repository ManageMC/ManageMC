package com.managemc.spigot.listener;

import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.listener.base.ListenersBase;
import com.managemc.spigot.listener.handler.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.*;

public class Listeners extends ListenersBase {

  private final PlayerCommandPreprocessEventHandler playerCommandPreprocessEventHandler;
  private final AsyncPlayerChatEventHandler asyncPlayerChatEventHandler;
  private final AsyncPlayerPreLoginEventHandler asyncPlayerPreLoginEventHandler;
  private final PlayerLoginEventHandler playerLoginEventHandler;
  private final PlayerQuitEventHandler playerQuitEventHandler;
  private final PlayerKickEventHandler playerKickEventHandler;

  public Listeners(SpigotPluginConfig config) {
    super(config.getLogging());
    this.playerCommandPreprocessEventHandler = new PlayerCommandPreprocessEventHandler(config);
    this.asyncPlayerChatEventHandler = new AsyncPlayerChatEventHandler(config);
    this.asyncPlayerPreLoginEventHandler = new AsyncPlayerPreLoginEventHandler(config);
    this.playerLoginEventHandler = new PlayerLoginEventHandler(config);
    this.playerQuitEventHandler = new PlayerQuitEventHandler(config);
    this.playerKickEventHandler = new PlayerKickEventHandler(config);
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent event) {
    runEvent(event, playerCommandPreprocessEventHandler);
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
    runEvent(event, asyncPlayerChatEventHandler);
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
    runEvent(event, asyncPlayerPreLoginEventHandler);
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPlayerLogin(PlayerLoginEvent event) {
    runEvent(event, playerLoginEventHandler);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    runEvent(event, playerQuitEventHandler);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerKick(PlayerKickEvent event) {
    runEvent(event, playerKickEventHandler);
  }
}
