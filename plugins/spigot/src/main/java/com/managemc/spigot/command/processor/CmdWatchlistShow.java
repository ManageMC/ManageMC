package com.managemc.spigot.command.processor;

import com.managemc.spigot.command.handler.CmdWatchlistShowHandler;
import com.managemc.spigot.command.handler.base.CommandHandlerAsync;
import com.managemc.spigot.command.processor.tabcompletion.NoOpTabCompleter;
import com.managemc.spigot.command.util.CommandProcessorAsync;
import com.managemc.spigot.command.util.CommandUsage;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.config.SpigotPluginConfig;
import org.bukkit.command.CommandSender;

public class CmdWatchlistShow extends CommandProcessorAsync {

  private final SpigotPluginConfig config;

  public CmdWatchlistShow(SpigotPluginConfig config) {
    super(
        config.getLogging(),
        CommandUsage.WATCHLIST_SHOW,
        new NoOpTabCompleter()
    );

    this.config = config;
  }

  @Override
  public CommandHandlerAsync getNewHandler(CommandSender sender, ProcessedCommandArguments args) {
    return new CmdWatchlistShowHandler(sender, args, config);
  }
}
