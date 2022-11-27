package com.managemc.plugins.command;

import com.managemc.plugins.logging.BukkitLogging;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandWrapping {

  public static String HARD_FAILURE_MESSAGE = ChatColor.DARK_RED + "Unexpected plugin exception";

  static Result wrap(
      WrappedFunction function,
      CommandSender sender,
      BukkitLogging logging
  ) {
    try {
      function.apply();
      return new Result(null, true);
    } catch (AbortCommand e) {
      sender.sendMessage(String.format("%s%s", ChatColor.RED, e.getMessage()));
      return new Result(e, false);
    } catch (Exception e) {
      logging.logStackTrace(e);
      sender.sendMessage(HARD_FAILURE_MESSAGE);
    }
    return new Result(null, false);
  }


  static class Result {

    @Getter
    private final AbortCommand error;
    @Getter
    private final boolean success;

    private Result(AbortCommand error, boolean success) {
      this.error = error;
      this.success = success;
    }
  }

  @FunctionalInterface
  interface WrappedFunction {
    void apply() throws Exception;
  }
}
