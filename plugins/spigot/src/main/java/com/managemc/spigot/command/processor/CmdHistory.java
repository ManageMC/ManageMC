package com.managemc.spigot.command.processor;

import com.managemc.spigot.command.handler.CmdHistoryHandler;
import com.managemc.spigot.command.handler.base.CommandHandlerAsync;
import com.managemc.spigot.command.processor.tabcompletion.CmdHistoryTabCompleter;
import com.managemc.spigot.command.util.CommandProcessorAsync;
import com.managemc.spigot.command.util.CommandUsage;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.config.SpigotPluginConfig;
import org.bukkit.command.CommandSender;

public class CmdHistory extends CommandProcessorAsync {

  private final SpigotPluginConfig config;

  public CmdHistory(SpigotPluginConfig config) {
    super(
        config.getLogging(),
        CommandUsage.PUNISHMENTS_GET,
        new CmdHistoryTabCompleter(config.getBukkitWrapper())
    );
    this.config = config;
  }

  @Override
  public CommandHandlerAsync getNewHandler(CommandSender sender, ProcessedCommandArguments args) {
    return new CmdHistoryHandler(sender, args, config);
  }
}
