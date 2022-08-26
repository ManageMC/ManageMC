package com.managemc.account_linker.command;

import com.managemc.plugins.logging.BukkitLogging;
import com.managemc.account_linker.service.AccountLinkingService;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdLinkAccount implements CommandExecutor {

  public static final String WRONG_NUM_ARGS = ChatColor.RED + "Wrong number of arguments";
  public static final String WRONG_CMD_MEDIUM = ChatColor.RED + "Only players have a use for this command";

  private final BukkitLogging logger;
  private final AccountLinkingService accountLinkingService;

  public CmdLinkAccount(BukkitLogging logger, AccountLinkingService accountLinkingService) {
    this.logger = logger;
    this.accountLinkingService = accountLinkingService;
  }

  @Override
  public boolean onCommand(
      @NonNull CommandSender sender,
      @NonNull Command command,
      @NonNull String label,
      String @NonNull [] args
  ) {
    try {
      if (args.length != 2) {
        sender.sendMessage(WRONG_NUM_ARGS);
        return false;
      }

      if (sender instanceof Player) {
        Player player = (Player) sender;
        accountLinkingService.linkPlayer(player, args[0], args[1]);
      }

      sender.sendMessage(WRONG_CMD_MEDIUM);
      return true;
    } catch (RuntimeException e) {
      logger.logStackTrace(e);
      sender.sendMessage(AccountLinkingService.UNEXPECTED_EXCEPTION);
      return true;
    }
  }
}
