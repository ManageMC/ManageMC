package com.managemc.spigot.command.processor;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.util.chat.formatter.WatchlistFormatter;
import com.managemc.spigot.util.permissions.PermissibleAction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.openapitools.client.model.PlayerWatchList;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CmdUnwatch extends CommandExecutorAsync {

  private final SpigotPluginConfig config;

  public CmdUnwatch(SpigotPluginConfig config) {
    super(config.getLogging());
    this.config = config;
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
    switch (args.length) {
      case 0:
        return getMatchingOnlinePlayers("");
      case 1:
        return getMatchingOnlinePlayers(args[0]);
      default:
        return null;
    }
  }

  private List<String> getMatchingOnlinePlayers(String lastArg) {
    return config.getBukkitWrapper().getOnlinePlayers().stream()
        .map(Player::getName)
        .filter(name -> name.toLowerCase().startsWith(lastArg.toLowerCase()))
        .sorted()
        .collect(Collectors.toList());
  }
}
