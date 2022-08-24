package com.managemc.spigot.listener.handler;

import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.listener.base.util.EventLogicHandler;
import com.managemc.spigot.listener.handler.helper.PlayerMuteCheckable;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerCommandPreprocessEventHandler extends PlayerMuteCheckable implements EventLogicHandler<PlayerCommandPreprocessEvent> {

  private final SpigotPluginConfig config;

  public PlayerCommandPreprocessEventHandler(SpigotPluginConfig config) {
    super(config.getMuteManager());
    this.config = config;
  }

  /*
      If the command is on the list of commands that player cannot use when muted,
      which is configured remotely and loaded on plugin startup, then we check whether
      the player at hand is muted. If so, the player may be sent a message indicating
      that they were muted. If the player is muted in any capacity on this server, then
      the command will not execute.
  */
  @Override
  public void handleEventLogic(PlayerCommandPreprocessEvent event) {
    if (!containsBlockableCommand(event.getMessage())) {
      return;
    }

    if (fetchMuteDataAndHandleMute(event.getPlayer()) != null) {
      event.setCancelled(true);
    }
  }

  /*
      We want to prevent these commands from running:
          - /tell
          - /tell things stuff

      But, importantly, this one should be possible:
          - /telly

      I don't think it's possible for the message not to start with a slash, but
      just in case we have accounted for that edge case by explicitly allowing it.
  */
  private boolean containsBlockableCommand(String commandMessage) {
    return config.getRemoteConfigService()
        .getRemoteConfig().getBlockedCommandsForMutedPlayers().stream()
        .anyMatch(cmd ->
            commandMessage.startsWith("/" + cmd + " ") ||
                commandMessage.equals("/" + cmd)
        );
  }
}
