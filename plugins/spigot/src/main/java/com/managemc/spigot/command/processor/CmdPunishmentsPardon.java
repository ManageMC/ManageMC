package com.managemc.spigot.command.processor;

import com.managemc.spigot.command.handler.CmdPunishmentsPardonHandler;
import com.managemc.spigot.command.handler.base.CommandHandlerAsync;
import com.managemc.spigot.command.processor.tabcompletion.CmdPunishmentsPardonTabCompleter;
import com.managemc.spigot.command.util.CommandProcessorAsync;
import com.managemc.spigot.command.util.CommandUsage;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.config.SpigotPluginConfig;
import org.bukkit.command.CommandSender;

public class CmdPunishmentsPardon extends CommandProcessorAsync {

  private final SpigotPluginConfig config;

  public CmdPunishmentsPardon(SpigotPluginConfig config) {
    super(
        config.getLogging(),
        CommandUsage.PUNISHMENTS_PARDON,
        new CmdPunishmentsPardonTabCompleter(config.getPardonablePunishmentData())
    );
    this.config = config;
  }

  @Override
  public CommandHandlerAsync getNewHandler(CommandSender sender, ProcessedCommandArguments args) {
    return new CmdPunishmentsPardonHandler(sender, args, config);
  }
}
