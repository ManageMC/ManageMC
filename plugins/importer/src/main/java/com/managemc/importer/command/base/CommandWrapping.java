package com.managemc.importer.command.base;

import com.managemc.plugins.logging.BukkitLogging;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandWrapping {

  public static boolean wrap(
      FunctionWrappedWithErrorHandling function,
      CommandSender sender,
      BukkitLogging logging,
      String usage
  ) {
    try {
      function.apply();
      return true;
    } catch (CommandValidationException e) {
      sender.sendMessage(String.format("%s%s", ChatColor.RED, e.getMessage()));
      if (e.isSyntactic()) {
        sender.sendMessage(String.format("%sUsage: %s", ChatColor.RED, usage));
      }
    } catch (Exception e) {
      logging.logStackTrace(e);
      sender.sendMessage(CommandBase.HARD_FAILURE_MESSAGE);
    }
    return false;
  }

  @FunctionalInterface
  public interface FunctionWrappedWithErrorHandling {
    void apply() throws Exception;
  }
}
