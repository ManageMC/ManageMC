package com.managemc.spigot.command.processor;

import com.managemc.spigot.command.handler.CmdBanHandler;
import com.managemc.spigot.command.handler.base.CommandHandlerAsync;
import com.managemc.spigot.command.processor.tabcompletion.CmdBanMuteTabCompleter;
import com.managemc.spigot.command.util.CommandProcessorAsync;
import com.managemc.spigot.command.util.CommandUsage;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.command.util.punishments.PunishmentFlags;
import com.managemc.spigot.config.SpigotPluginConfig;
import org.bukkit.command.CommandSender;

public class CmdBan extends CommandProcessorAsync {

  private final SpigotPluginConfig config;

  public CmdBan(SpigotPluginConfig config) {
    super(
        config.getLogging(),
        CommandUsage.BANS_CREATE,
        new CmdBanMuteTabCompleter(config.getBukkitWrapper()),
        PunishmentFlags.FLAGS
    );
    this.config = config;
  }

  @Override
  public CommandHandlerAsync getNewHandler(CommandSender sender, ProcessedCommandArguments args) {
    return new CmdBanHandler(sender, args, config);
  }
}
