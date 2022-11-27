package com.managemc.spigot.util.permissions;

import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum PermissibleAction {
  BAN_PLAYER(Permission.BAN_IN_GAME, Permission.BAN_WEB),
  MUTE_PLAYER(Permission.MUTE_IN_GAME, Permission.MUTE_WEB),
  WARN_PLAYER(Permission.WARN_IN_GAME, Permission.WARN_WEB),
  IP_BAN_PLAYER(Permission.IP_BAN_IN_GAME, Permission.IP_BAN_WEB),
  SHADOW_PUNISHMENTS(Permission.PUNISHMENTS_SHADOW),
  MEDIUM_PUNISHMENTS(Permission.PUNISHMENTS_LENGTH_MED),
  LONG_PUNISHMENTS(Permission.PUNISHMENTS_LENGTH_LONG),
  PERMANENT_PUNISHMENTS(Permission.PUNISHMENTS_LENGTH_PERM),
  FETCH_PUNISHMENT_HISTORY(Permission.PUNISHMENTS_VIEW),
  MANAGE_WATCHLIST(Permission.MANAGE_WATCHLIST),

  // composite actions
  PARDON_POSSIBLY(BAN_PLAYER, MUTE_PLAYER, WARN_PLAYER, IP_BAN_PLAYER),
  ;

  @Getter
  private final Set<String> permissions;

  PermissibleAction(Permission... permissions) {
    this.permissions = new HashSet<>();
    this.permissions.add(Permission.OWNER.getName());
    this.permissions.add(Permission.ADMIN.getName());

    Arrays.stream(permissions)
        .map(Permission::getName)
        .forEach(this.permissions::add);
  }

  PermissibleAction(PermissibleAction... actions) {
    this.permissions = new HashSet<>();
    this.permissions.add(Permission.OWNER.getName());
    this.permissions.add(Permission.ADMIN.getName());

    for (PermissibleAction action : actions) {
      this.permissions.addAll(action.permissions);
    }
  }

  public boolean isAllowed(CommandSender sender) {
    if (sender instanceof Player) {
      Player player = (Player) sender;
      String perm = this.permissions.stream().filter(player::hasPermission).findFirst().orElse(null);
      return perm != null;
    }
    return true;
  }
}
