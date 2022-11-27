package com.managemc.spigot.command;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.util.chat.formatter.WatchlistFormatter;
import com.managemc.spigot.util.permissions.PermissibleAction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.openapitools.client.model.PlayerWatchList;

import java.util.List;

public class CmdWatchlist extends CommandExecutorAsync {

  private final SpigotPluginConfig config;

  public CmdWatchlist(SpigotPluginConfig config) {
    super(config.getLogging());
    this.config = config;
  }

  private Player playerSender;

  @Override
  protected void preProcessCommand(CommandSender sender, String[] args) throws Exception {
    CommandAssertions.checkPermissions(PermissibleAction.MANAGE_WATCHLIST, sender);
    playerSender = CommandAssertions.assertPlayerSender(sender);
    CommandAssertions.assertArgsLength(args, 0);
  }

  @Override
  protected void onCommandAsync(CommandSender sender) throws Exception {
    PlayerWatchList watchList = config.getWatchlistService().getWatchlist(playerSender);
    sender.sendMessage(new WatchlistFormatter(watchList).format());
  }

  @Override
  protected List<String> onTabComplete(CommandSender sender, String[] args) {
    return null;
  }
}
