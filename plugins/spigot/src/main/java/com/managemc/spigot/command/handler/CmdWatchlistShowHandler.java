package com.managemc.spigot.command.handler;

import com.managemc.spigot.command.handler.base.CommandHandlerAsync;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.util.chat.formatter.WatchlistFormatter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.openapitools.client.model.PlayerWatchList;

public class CmdWatchlistShowHandler extends CommandHandlerAsync {

  private final SpigotPluginConfig config;

  public CmdWatchlistShowHandler(CommandSender sender, ProcessedCommandArguments args, SpigotPluginConfig config) {
    super(sender, args);
    this.config = config;
  }

  private Player playerSender;

  @Override
  public void preProcessCommand() throws Exception {
    playerSender = CommandAssertions.assertPlayerSender(sender);
    CommandAssertions.assertArgsLength(args.getArgs(), 0);
  }

  @Override
  public void processCommandAsync() throws Exception {
    PlayerWatchList watchList = config.getWatchlistService().getWatchlist(playerSender);
    sender.sendMessage(new WatchlistFormatter(watchList).format());
  }
}
