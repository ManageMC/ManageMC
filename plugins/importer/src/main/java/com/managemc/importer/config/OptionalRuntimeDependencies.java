package com.managemc.importer.config;

import lombok.Getter;

public enum OptionalRuntimeDependencies {
  ADVANCED_BAN("me.leoko.advancedban.bukkit.BukkitMain"),
  MAX_BANS_PLUS("org.maxgamer.maxbans.MaxBansPlus");

  @Getter
  private final String mainClassName;

  OptionalRuntimeDependencies(String mainClassName) {
    this.mainClassName = mainClassName;
  }

  public boolean isAvailable() {
    return softDependencyFound(mainClassName);
  }

  private boolean softDependencyFound(String className) {
    try {
      Class.forName(className);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}
