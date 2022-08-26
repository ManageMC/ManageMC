package com.managemc.importer.command;

import com.managemc.importer.command.base.AsyncCommandHandler;
import com.managemc.importer.command.base.CommandBaseAsync;
import com.managemc.importer.command.base.CommandBuilder;
import com.managemc.importer.command.handler.CmdImportHandler;
import com.managemc.importer.config.ManageMCImportPluginConfig;
import com.managemc.plugins.logging.BukkitLogging;
import org.bukkit.command.CommandSender;

public class CmdImport extends CommandBaseAsync {

  static final String USAGE = "/import <source [VANILLA | ADVANCED_BAN | MAX_BANS_PLUS]>";
  static final String DESCRIPTION = "Asynchronously import punishments to ManageMC from a supported external source";

  private static final CommandBuilder COMMAND_BUILDER = CommandBuilder.builder()
      .description(DESCRIPTION)
      .name("import")
      .usage(USAGE)
      .build();

  private final ManageMCImportPluginConfig config;

  public CmdImport(BukkitLogging logging, ManageMCImportPluginConfig config) {
    super(COMMAND_BUILDER, logging);
    this.config = config;
  }

  @Override
  public AsyncCommandHandler getNewHandler(CommandSender sender, String[] args) {
    return CmdImportHandler.defaultInstance(sender, args, config);
  }
}
