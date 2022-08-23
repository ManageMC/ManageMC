package com.managemc.plugins.logging;

import org.bukkit.Bukkit;

import java.util.logging.Level;

public class BukkitLogging {

  private static final String LOG_STACK_TRACE_MESSAGE = "An error occurred. Please include the following stack trace if you file a bug report:";

  private final String loggerPrefix;

  public BukkitLogging(String loggerPrefix) {
    this.loggerPrefix = loggerPrefix;
  }

  public void logInfo(String message) {
    Bukkit.getLogger().info(ConsoleColors.ANSI_BLUE + wrap(message));
  }

  public void logWarning(String message) {
    Bukkit.getLogger().warning(ConsoleColors.ANSI_YELLOW + wrap(message));
  }

  public void logStackTrace(Throwable error) {
    Bukkit.getLogger().log(Level.WARNING, ConsoleColors.ANSI_YELLOW + wrap(LOG_STACK_TRACE_MESSAGE), error);
  }

  public void logSevere(String message) {
    Bukkit.getLogger().severe(ConsoleColors.ANSI_RED + wrap(message));
  }


  private String wrap(String message) {
    return String.format("%s%s %s%s", ConsoleColors.ANSI_BOLD, loggerPrefix, message, ConsoleColors.ANSI_RESET);
  }
}
