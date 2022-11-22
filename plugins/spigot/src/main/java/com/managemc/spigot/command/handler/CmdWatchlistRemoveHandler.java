package com.managemc.spigot.command.handler;

import com.managemc.spigot.command.handler.base.CommandHandlerAsync;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.util.chat.formatter.WatchlistFormatter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.openapitools.client.model.PlayerWatchList;

import java.util.UUID;

public class CmdWatchlistRemoveHandler extends CommandHandlerAsync {

  private final SpigotPluginConfig config;

  public CmdWatchlistRemoveHandler(CommandSender sender, ProcessedCommandArguments args, SpigotPluginConfig config) {
    super(sender, args);
    this.config = config;
  }

  @Override
  public void preProcessCommand() throws Exception {
    playerSender = CommandAssertions.assertPlayerSender(sender);
    CommandAssertions.assertArgsLength(args.getArgs(), 1);
    CommandAssertions.assertValidUsername(args.get(0));
  }

  private Player playerSender;

  @Override
  public void processCommandAsync() throws Exception {
    UUID uuid = CommandAssertions.usernameToUuidBlocking(config.getUuidResolver(), args.get(0));
    PlayerWatchList watchList = config.getWatchlistService().removeFromWatchlist(playerSender, uuid);
    sender.sendMessage(new WatchlistFormatter(watchList).format());
  }
}
