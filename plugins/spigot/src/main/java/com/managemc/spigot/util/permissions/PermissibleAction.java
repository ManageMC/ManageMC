package com.managemc.spigot.util.permissions;

import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum PermissibleAction {
  BAN_PLAYER(Permission.PLAYERS_ALL, Permission.PUNISHMENTS_ALL, Permission.BAN_WEB, Permission.BAN_IN_GAME),
  MUTE_PLAYER(Permission.PLAYERS_ALL, Permission.PUNISHMENTS_ALL, Permission.MUTE_WEB, Permission.MUTE_IN_GAME),
  WARN_PLAYER(Permission.PLAYERS_ALL, Permission.PUNISHMENTS_ALL, Permission.WARN_WEB, Permission.WARN_IN_GAME),
  IP_BAN_PLAYER(Permission.PLAYERS_ALL, Permission.PUNISHMENTS_ALL, Permission.IP_BAN_WEB, Permission.IP_BAN_IN_GAME),
  SHADOW_PUNISHMENTS(Permission.PLAYERS_ALL, Permission.PUNISHMENTS_ALL, Permission.PUNISHMENTS_SHADOW),
  MEDIUM_PUNISHMENTS(Permission.PLAYERS_ALL, Permission.PUNISHMENTS_ALL, Permission.PUNISHMENTS_LENGTH_PERM, Permission.PUNISHMENTS_LENGTH_LONG, Permission.PUNISHMENTS_LENGTH_MED),
  LONG_PUNISHMENTS(Permission.PLAYERS_ALL, Permission.PUNISHMENTS_ALL, Permission.PUNISHMENTS_LENGTH_PERM, Permission.PUNISHMENTS_LENGTH_LONG),
  PERMANENT_PUNISHMENTS(Permission.PLAYERS_ALL, Permission.PUNISHMENTS_ALL, Permission.PUNISHMENTS_LENGTH_PERM),
  FETCH_PUNISHMENT_HISTORY(Permission.PLAYERS_ALL, Permission.PUNISHMENTS_ALL, Permission.PUNISHMENTS_VIEW),
  MANAGE_WATCHLIST(Permission.MANAGE_ALL, Permission.MANAGE_WATCHLIST),

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
