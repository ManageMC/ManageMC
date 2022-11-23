package com.managemc.spigot.command.processor;

import com.managemc.spigot.command.handler.CmdWarnHandler;
import com.managemc.spigot.command.handler.base.CommandHandlerAsync;
import com.managemc.spigot.command.processor.tabcompletion.CmdWarnTabCompleter;
import com.managemc.spigot.command.util.CommandProcessorAsync;
import com.managemc.spigot.command.util.CommandUsage;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.config.SpigotPluginConfig;
import org.bukkit.command.CommandSender;

public class CmdWarn extends CommandProcessorAsync {

  private final SpigotPluginConfig config;

  public CmdWarn(SpigotPluginConfig config) {
    super(
        config.getLogging(),
        CommandUsage.WARNINGS_CREATE,
        new CmdWarnTabCompleter(config.getBukkitWrapper())
    );
    this.config = config;
  }

  @Override
  public CommandHandlerAsync getNewHandler(CommandSender sender, ProcessedCommandArguments args) {
    return new CmdWarnHandler(sender, args, config);
  }
}
