package com.managemc.spigot.command;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.tabcompletion.OnlinePlayersTabCompleter;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.util.chat.formatter.WatchlistFormatter;
import com.managemc.spigot.util.permissions.PermissibleAction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.openapitools.client.model.PlayerWatchList;

import java.util.List;
import java.util.UUID;

public class CmdUnwatch extends CommandExecutorAsync {

  private final SpigotPluginConfig config;
  private final OnlinePlayersTabCompleter tabCompleter;

  public CmdUnwatch(SpigotPluginConfig config) {
    super(config.getLogging());
    this.config = config;
    this.tabCompleter = new OnlinePlayersTabCompleter(config.getBukkitWrapper());
  }

  private Player playerSender;
  private String username;

  @Override
  protected void preProcessCommand(CommandSender sender, String[] args) throws Exception {
    CommandAssertions.checkPermissions(PermissibleAction.MANAGE_WATCHLIST, sender);
    playerSender = CommandAssertions.assertPlayerSender(sender);
    CommandAssertions.assertArgsLength(args, 1);
    username = CommandAssertions.assertValidUsername(args[0]);
  }

  @Override
  protected void onCommandAsync(CommandSender sender) throws Exception {
    UUID uuid = CommandAssertions.usernameToUuidBlocking(config.getUuidResolver(), username);
    PlayerWatchList watchList = config.getWatchlistService().removeFromWatchlist(playerSender, uuid);
    sender.sendMessage(new WatchlistFormatter(watchList).format());
  }

  @Override
  protected List<String> onTabComplete(CommandSender sender, String[] args) {
    if (PermissibleAction.MANAGE_WATCHLIST.isAllowed(sender)) {
      return tabCompleter.onTabComplete(args);
    }
    return null;
  }
}
