package com.managemc.spigot.command.processor;

import com.managemc.spigot.command.handler.CmdMutesCreateHandler;
import com.managemc.spigot.command.handler.base.CommandHandlerAsync;
import com.managemc.spigot.command.processor.tabcompletion.BanOrMuteCommandTabCompleter;
import com.managemc.spigot.command.util.CommandProcessorAsync;
import com.managemc.spigot.command.util.CommandUsage;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.command.util.punishments.PunishmentFlags;
import com.managemc.spigot.config.SpigotPluginConfig;
import org.bukkit.command.CommandSender;

public class CmdMutesCreate extends CommandProcessorAsync {

  private final SpigotPluginConfig config;

  public CmdMutesCreate(SpigotPluginConfig config) {
    super(
        config.getLogging(),
        CommandUsage.MUTES_CREATE,
        new BanOrMuteCommandTabCompleter(config.getBukkitWrapper()),
        PunishmentFlags.FLAGS
    );
    this.config = config;
  }

  @Override
  public CommandHandlerAsync getNewHandler(CommandSender sender, ProcessedCommandArguments args) {
    return new CmdMutesCreateHandler(sender, args, config);
  }
}
