package com.managemc.spigot.command.processor;

import com.managemc.spigot.command.handler.CmdPlayersCreateHandler;
import com.managemc.spigot.command.handler.base.CommandHandlerAsync;
import com.managemc.spigot.command.util.CommandProcessorAsync;
import com.managemc.spigot.command.util.CommandUsage;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.config.SpigotPluginConfig;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class CmdPlayersCreate extends CommandProcessorAsync {

  private final SpigotPluginConfig config;

  /**
   * Specifically do NOT tab complete anything. All online players exist already in our db unless there
   * was a runtime error, so the user is probably looking for an offline user which we have no knowledge
   * about.
   */
  public CmdPlayersCreate(SpigotPluginConfig config) {
    super(config.getLogging(), CommandUsage.PLAYERS_CREATE, (sender, args) -> new ArrayList<>());
    this.config = config;
  }

  @Override
  public CommandHandlerAsync getNewHandler(CommandSender sender, ProcessedCommandArguments args) {
    return new CmdPlayersCreateHandler(sender, args, config);
  }
}
