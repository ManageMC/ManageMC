package com.managemc.spigot.command.processor;

import com.managemc.spigot.command.handler.CmdPunishmentsGetHandler;
import com.managemc.spigot.command.handler.base.CommandHandlerAsync;
import com.managemc.spigot.command.processor.tabcompletion.CmdPunishmentsGetTabCompleter;
import com.managemc.spigot.command.util.CommandProcessorAsync;
import com.managemc.spigot.command.util.CommandUsage;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.config.SpigotPluginConfig;
import org.bukkit.command.CommandSender;

public class CmdPunishmentsGet extends CommandProcessorAsync {

  private final SpigotPluginConfig config;

  public CmdPunishmentsGet(SpigotPluginConfig config) {
    super(
        config.getLogging(),
        CommandUsage.PUNISHMENTS_GET,
        new CmdPunishmentsGetTabCompleter(config.getBukkitWrapper())
    );
    this.config = config;
  }

  @Override
  public CommandHandlerAsync getNewHandler(CommandSender sender, ProcessedCommandArguments args) {
    return new CmdPunishmentsGetHandler(sender, args, config);
  }
}
