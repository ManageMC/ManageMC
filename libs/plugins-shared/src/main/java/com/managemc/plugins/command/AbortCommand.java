package com.managemc.plugins.command;

import lombok.Getter;

/**
 * Throwing this error within the handler methods of
 * #{@link CommandExecutorSync} and #{@link CommandExecutorAsync}
 * will immediately terminate command processing and relay the
 * provided error message back to the command sender. The usage
 * message will be included second if specified.
 */
public class AbortCommand extends IllegalArgumentException {

  @Getter
  private final boolean syntactic;

  private AbortCommand(String message, boolean syntactic) {
    super(message);
    this.syntactic = syntactic;
  }

  public static AbortCommand withUsageMessage(String message) {
    return new AbortCommand(message, true);
  }

  public static AbortCommand withoutUsageMessage(String message) {
    return new AbortCommand(message, false);
  }
}
