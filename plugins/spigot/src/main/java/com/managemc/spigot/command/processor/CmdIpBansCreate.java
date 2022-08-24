package com.managemc.spigot.command.processor;

import com.managemc.spigot.command.handler.CmdIpBansCreateHandler;
import com.managemc.spigot.command.handler.base.CommandHandlerAsync;
import com.managemc.spigot.command.processor.tabcompletion.CmdIpBansCreateTabCompleter;
import com.managemc.spigot.command.util.CommandProcessorAsync;
import com.managemc.spigot.command.util.CommandUsage;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.command.util.punishments.PunishmentFlags;
import com.managemc.spigot.config.SpigotPluginConfig;
import org.bukkit.command.CommandSender;

public class CmdIpBansCreate extends CommandProcessorAsync {

  private final SpigotPluginConfig config;

  public CmdIpBansCreate(SpigotPluginConfig config) {
    super(
        config.getLogging(),
        CommandUsage.IPBANS_CREATE,
        new CmdIpBansCreateTabCompleter(),
        PunishmentFlags.FLAGS
    );
    this.config = config;
  }

  @Override
  public CommandHandlerAsync getNewHandler(CommandSender sender, ProcessedCommandArguments args) {
    return new CmdIpBansCreateHandler(sender, args, config);
  }
}
