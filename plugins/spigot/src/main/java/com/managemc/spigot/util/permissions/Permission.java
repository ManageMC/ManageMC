package com.managemc.spigot.util.permissions;

import lombok.Getter;

public enum Permission {
  OWNER("com.managemc.staff.owner"),
  ADMIN("com.managemc.staff.admin"),
  BAN_WEB("com.managemc.staff.punishments.bans.create"),
  BAN_IN_GAME("com.managemc.staff.punishments.bans.create-ingame"),
  MUTE_WEB("com.managemc.staff.punishments.mutes.create"),
  MUTE_IN_GAME("com.managemc.staff.punishments.mutes.create-ingame"),
  WARN_WEB("com.managemc.staff.punishments.warnings.create"),
  WARN_IN_GAME("com.managemc.staff.punishments.warnings.create-ingame"),
  IP_BAN_WEB("com.managemc.staff.punishments.ip-bans.create"),
  IP_BAN_IN_GAME("com.managemc.staff.punishments.ip-bans.create-ingame"),
  PUNISHMENTS_VIEW("com.managemc.staff.punishments.view"),
  PUNISHMENTS_SHADOW("com.managemc.staff.punishments.shadow.manage"),
  PUNISHMENTS_LENGTH_MED("com.managemc.staff.punishments.length.medium"),
  PUNISHMENTS_LENGTH_LONG("com.managemc.staff.punishments.length.long"),
  PUNISHMENTS_LENGTH_PERM("com.managemc.staff.punishments.length.permanent"),
  ;

  @Getter
  private final String name;

  Permission(String name) {
    this.name = name;
  }
}
