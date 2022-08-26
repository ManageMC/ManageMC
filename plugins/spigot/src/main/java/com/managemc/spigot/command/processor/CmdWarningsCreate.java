package com.managemc.spigot.command.processor;

import com.managemc.spigot.command.handler.CmdWarningsCreateHandler;
import com.managemc.spigot.command.handler.base.CommandHandlerAsync;
import com.managemc.spigot.command.processor.tabcompletion.CmdWarningsCreateTabCompleter;
import com.managemc.spigot.command.util.CommandProcessorAsync;
import com.managemc.spigot.command.util.CommandUsage;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.config.SpigotPluginConfig;
import org.bukkit.command.CommandSender;

public class CmdWarningsCreate extends CommandProcessorAsync {

  private final SpigotPluginConfig config;

  public CmdWarningsCreate(SpigotPluginConfig config) {
    super(
        config.getLogging(),
        CommandUsage.WARNINGS_CREATE,
        new CmdWarningsCreateTabCompleter(config.getBukkitWrapper())
    );
    this.config = config;
  }

  @Override
  public CommandHandlerAsync getNewHandler(CommandSender sender, ProcessedCommandArguments args) {
    return new CmdWarningsCreateHandler(sender, args, config);
  }
}
