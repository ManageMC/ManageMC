package com.managemc.importer.command.base;

import com.managemc.plugins.logging.BukkitLogging;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

public abstract class CommandBaseSync extends CommandBase {

  public CommandBaseSync(CommandBuilder builder, BukkitLogging logging) {
    super(builder, logging);
  }

  @Override
  public final boolean execute(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
    CommandWrapping.wrap(() -> this.onCommand(sender, args), sender, logging, getUsage());
    return true;
  }

  public abstract void onCommand(CommandSender sender, String[] args) throws Exception;
}
