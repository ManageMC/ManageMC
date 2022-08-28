package com.managemc.plugins.logging;

import org.bukkit.Bukkit;

import java.util.logging.Level;

public class BukkitLogging {

  private static final String LOG_STACK_TRACE_MESSAGE = "An error occurred. Please include the following stack trace if you file a bug report:";

  private final String loggerPrefix;

  public BukkitLogging(String pluginName) {
    this.loggerPrefix = String.format(
        "%s%s[%s%s%s]%s",
        ConsoleColors.ANSI_BOLD,
        ConsoleColors.ANSI_PURPLE,
        ConsoleColors.ANSI_CYAN,
        pluginName,
        ConsoleColors.ANSI_PURPLE,
        ConsoleColors.ANSI_RESET
    );
  }

  public void logInfo(String message) {
    Bukkit.getLogger().info(wrap(message, ConsoleColors.ANSI_BLUE));
  }

  public void logWarning(String message) {
    Bukkit.getLogger().warning(wrap(message, ConsoleColors.ANSI_YELLOW));
  }

  public void logStackTrace(Throwable error) {
    Bukkit.getLogger().log(Level.WARNING, wrap(LOG_STACK_TRACE_MESSAGE, ConsoleColors.ANSI_YELLOW), error);
  }

  public void logSevere(String message) {
    Bukkit.getLogger().severe(wrap(message, ConsoleColors.ANSI_RED));
  }


  private String wrap(String message, String color) {
    return String.format("%s%s%s %s%s", loggerPrefix, color, ConsoleColors.ANSI_BOLD, message, ConsoleColors.ANSI_RESET);
  }
}
