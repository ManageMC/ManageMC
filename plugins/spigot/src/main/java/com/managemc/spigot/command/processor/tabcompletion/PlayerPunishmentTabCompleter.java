package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.plugins.bukkit.BukkitWrapper;
import com.managemc.spigot.command.util.punishments.PunishmentFlags;
import com.managemc.spigot.util.permissions.PermissibleAction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerPunishmentTabCompleter {

  private final BukkitWrapper bukkitWrapper;
  private final FlagsTabCompleter flagsTabCompleter = new FlagsTabCompleter()
      .registerIncompatibility(PunishmentFlags.BROADCAST_FLAG, PunishmentFlags.PRIVATE_FLAG)
      .registerIncompatibility(PunishmentFlags.BROADCAST_FLAG, PunishmentFlags.SHADOW_FLAG)
      .registerIncompatibility(PunishmentFlags.PRIVATE_FLAG, PunishmentFlags.SHADOW_FLAG)
      .registerPermissionRequirement(PunishmentFlags.SHADOW_FLAG, PermissibleAction.SHADOW_PUNISHMENTS);

  public PlayerPunishmentTabCompleter(BukkitWrapper bukkitWrapper) {
    this.bukkitWrapper = bukkitWrapper;
  }

  public List<String> onTabComplete(CommandSender sender, String[] args) {
    switch (args.length) {
      case 0:
        return getMatchingOnlinePlayers("");
      case 1:
        return getMatchingOnlinePlayers(args[0]);
      default:
        return flagsTabCompleter.onTabComplete(PunishmentFlags.FLAGS, sender, args);
    }
  }

  private List<String> getMatchingOnlinePlayers(String lastArg) {
    return bukkitWrapper.getOnlinePlayers().stream()
        .map(Player::getName)
        .filter(name -> name.toLowerCase().contains(lastArg.toLowerCase()))
        .sorted()
        .collect(Collectors.toList());
  }
}
