package com.managemc.spigot.command.processor;

import com.managemc.spigot.command.handler.CmdPardonHandler;
import com.managemc.spigot.command.handler.base.CommandHandlerAsync;
import com.managemc.spigot.command.processor.tabcompletion.CmdPardonTabCompleter;
import com.managemc.spigot.command.util.CommandProcessorAsync;
import com.managemc.spigot.command.util.CommandUsage;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.config.SpigotPluginConfig;
import org.bukkit.command.CommandSender;

public class CmdPardon extends CommandProcessorAsync {

  private final SpigotPluginConfig config;

  public CmdPardon(SpigotPluginConfig config) {
    super(
        config.getLogging(),
        CommandUsage.PUNISHMENTS_PARDON,
        new CmdPardonTabCompleter(config.getPardonablePunishmentData())
    );
    this.config = config;
  }

  @Override
  public CommandHandlerAsync getNewHandler(CommandSender sender, ProcessedCommandArguments args) {
    return new CmdPardonHandler(sender, args, config);
  }
}
