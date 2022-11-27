package com.managemc.linker.command;

import com.managemc.linker.service.AccountLinkingService;
import com.managemc.plugins.command.AbortCommand;
import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.plugins.logging.BukkitLogging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CmdLinkAccount extends CommandExecutorAsync {

  public static final String WRONG_NUM_ARGS = "Wrong number of arguments";
  public static final String WRONG_CMD_MEDIUM = "Only players have a use for this command";

  private final AccountLinkingService accountLinkingService;

  public CmdLinkAccount(BukkitLogging logger, AccountLinkingService accountLinkingService) {
    super(logger);
    this.accountLinkingService = accountLinkingService;
  }

  private String token;
  private Player playerSender;

  @Override
  protected void preProcessCommand(CommandSender sender, String[] args) {
    if (!(sender instanceof Player)) {
      throw AbortCommand.withoutUsageMessage(WRONG_CMD_MEDIUM);
    }

    if (args.length != 1) {
      throw AbortCommand.withUsageMessage(WRONG_NUM_ARGS);
    }

    token = args[0];
    playerSender = (Player) sender;
  }

  @Override
  protected void onCommandAsync(CommandSender sender) throws Exception {
    accountLinkingService.linkPlayer(playerSender, token);
  }

  @Override
  protected List<String> onTabComplete(CommandSender sender, String[] args) {
    return null;
  }
}
