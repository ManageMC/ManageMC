package com.managemc.spigot.command.processor;

import com.managemc.spigot.command.handler.CmdWatchlistRemoveHandler;
import com.managemc.spigot.command.handler.base.CommandHandlerAsync;
import com.managemc.spigot.command.processor.tabcompletion.PlayerTabCompleter;
import com.managemc.spigot.command.util.CommandProcessorAsync;
import com.managemc.spigot.command.util.CommandUsage;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.config.SpigotPluginConfig;
import org.bukkit.command.CommandSender;

public class CmdWatchlistRemove extends CommandProcessorAsync {

  private final SpigotPluginConfig config;

  public CmdWatchlistRemove(SpigotPluginConfig config) {
    super(
        config.getLogging(),
        CommandUsage.WATCHLIST_REMOVE,
        new PlayerTabCompleter(config.getBukkitWrapper())
    );

    this.config = config;
  }

  @Override
  public CommandHandlerAsync getNewHandler(CommandSender sender, ProcessedCommandArguments args) {
    return new CmdWatchlistRemoveHandler(sender, args, config);
  }
}
