package com.managemc.spigot.util.permissions;

import lombok.Getter;

public enum Permission {
  OWNER("managemc.special.owner"),
  ADMIN("managemc.special.admin"),

  PLAYERS_ALL("managemc.players.*"),
  MANAGE_ALL("managemc.manage.*"),
  CONFIG_ALL("managemc.config.*"),
  PUNISHMENTS_ALL("managemc.players.punishments.*"),
  BAN_WEB("managemc.players.punishments.bans.*"),
  BAN_IN_GAME("managemc.players.punishments.bans.ingame"),
  MUTE_WEB("managemc.players.punishments.mutes.*"),
  MUTE_IN_GAME("managemc.players.punishments.mutes.ingame"),
  WARN_WEB("managemc.players.punishments.warnings.*"),
  WARN_IN_GAME("managemc.players.punishments.warnings.ingame"),
  IP_BAN_WEB("managemc.players.punishments.ip-bans.*"),
  IP_BAN_IN_GAME("managemc.players.punishments.ip-bans.ingame"),
  PUNISHMENTS_VIEW("managemc.config.punishments.view"),
  PUNISHMENTS_SHADOW("managemc.players.punishments.shadow"),
  PUNISHMENTS_LENGTH_MED("managemc.players.punishments.length.medium"),
  PUNISHMENTS_LENGTH_LONG("managemc.players.punishments.length.long"),
  PUNISHMENTS_LENGTH_PERM("managemc.players.punishments.length.permanent"),
  MANAGE_WATCHLIST("managemc.manage.watchlist"),
  ;

  @Getter
  private final String name;

  Permission(String name) {
    this.name = name;
  }
}
