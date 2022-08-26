package com.managemc.spigot.command.util;

import lombok.Getter;
import lombok.NonNull;

public class CommandFlag {

  @Getter
  private final String flag;
  @Getter
  private final String alias;
  @Getter
  private final boolean expectingArgument;

  public CommandFlag(@NonNull String flag, String alias, boolean expectingArgument) {
    if (flag.contains("-") || (alias != null && alias.contains("-"))) {
      throw new IllegalArgumentException();
    }
    this.flag = flag;
    this.alias = alias;
    this.expectingArgument = expectingArgument;
  }

  public static CommandFlag defaultFlag(@NonNull String flag) {
    return new CommandFlag(flag, flag.substring(0, 1), false);
  }

  public static CommandFlag argumentFlag(@NonNull String flag) {
    return new CommandFlag(flag, flag.substring(0, 1), true);
  }

  @Override
  public String toString() {
    return "--" + flag + (expectingArgument ? "=" : "");
  }

  public String aliasToString() {
    return "-" + alias + (expectingArgument ? "=" : "");
  }
}
