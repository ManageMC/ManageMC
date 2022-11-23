package com.managemc.spigot.command.processor;

import com.managemc.spigot.command.handler.CmdCreatePlayerHandler;
import com.managemc.spigot.command.handler.base.CommandHandlerAsync;
import com.managemc.spigot.command.processor.tabcompletion.NoOpTabCompleter;
import com.managemc.spigot.command.util.CommandProcessorAsync;
import com.managemc.spigot.command.util.CommandUsage;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.config.SpigotPluginConfig;
import org.bukkit.command.CommandSender;

public class CmdCreatePlayer extends CommandProcessorAsync {

  private final SpigotPluginConfig config;

  /**
   * Specifically do NOT tab complete anything. All online players exist already in our db unless there
   * was a runtime error, so the user is probably looking for an offline user which we have no knowledge
   * about.
   */
  public CmdCreatePlayer(SpigotPluginConfig config) {
    super(config.getLogging(), CommandUsage.PLAYERS_CREATE, new NoOpTabCompleter());
    this.config = config;
  }

  @Override
  public CommandHandlerAsync getNewHandler(CommandSender sender, ProcessedCommandArguments args) {
    return new CmdCreatePlayerHandler(sender, args, config);
  }
}
