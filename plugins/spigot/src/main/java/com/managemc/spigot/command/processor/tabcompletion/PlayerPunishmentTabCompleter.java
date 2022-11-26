package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.plugins.bukkit.BukkitWrapper;
import com.managemc.spigot.command.util.punishments.PunishmentFlags;
import com.managemc.spigot.util.permissions.PermissibleAction;
import org.bukkit.command.CommandSender;

import java.util.List;

public class PlayerPunishmentTabCompleter {

  private final OnlinePlayersTabCompleter onlinePlayersTabCompleter;
  private final FlagsTabCompleter flagsTabCompleter = new FlagsTabCompleter()
      .registerIncompatibility(PunishmentFlags.BROADCAST_FLAG, PunishmentFlags.PRIVATE_FLAG)
      .registerIncompatibility(PunishmentFlags.BROADCAST_FLAG, PunishmentFlags.SHADOW_FLAG)
      .registerIncompatibility(PunishmentFlags.PRIVATE_FLAG, PunishmentFlags.SHADOW_FLAG)
      .registerPermissionRequirement(PunishmentFlags.SHADOW_FLAG, PermissibleAction.SHADOW_PUNISHMENTS);

  public PlayerPunishmentTabCompleter(BukkitWrapper bukkitWrapper) {
    this.onlinePlayersTabCompleter = new OnlinePlayersTabCompleter(bukkitWrapper);
  }

  public List<String> onTabComplete(CommandSender sender, String[] args) {
    switch (args.length) {
      case 0:
      case 1:
        return onlinePlayersTabCompleter.onTabComplete(args);
      default:
        return flagsTabCompleter.onTabComplete(PunishmentFlags.FLAGS, sender, args);
    }
  }
}
