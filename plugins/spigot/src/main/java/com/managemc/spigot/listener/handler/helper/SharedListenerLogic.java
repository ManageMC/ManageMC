package com.managemc.spigot.listener.handler.helper;

import com.managemc.api.ApiException;
import com.managemc.spigot.config.SpigotPluginConfig;
import org.bukkit.entity.Player;

public class SharedListenerLogic {

  /**
   * If a player is kicked, PlayerKickEvent is called but PlayerQuitEvent is not.
   * Having this method here forces future developers to consider whether logic
   * added to one handler or the other belongs in both.
   */
  public static void onPlayerLeaveServer(SpigotPluginConfig config, Player player) {
    config.getPermissionsManager().clearPermissions(player);

    new Thread(() -> {
      try {
        config.getLoginLogoutService().onLogout(player.getUniqueId());
      } catch (ApiException e) {
        config.getLogging().logStackTrace(e);
      }
    }).start();
  }
}
