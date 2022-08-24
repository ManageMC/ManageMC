package com.managemc.importer.command.base;

import com.managemc.plugins.logging.BukkitLogging;
import org.bukkit.ChatColor;
import org.bukkit.command.defaults.BukkitCommand;

import java.util.Collections;

public abstract class CommandBase extends BukkitCommand {

  public static final String HARD_FAILURE_MESSAGE = String.format("%sUnexpected plugin exception", ChatColor.DARK_RED);

  protected final BukkitLogging logging;

  public CommandBase(CommandBuilder builder, BukkitLogging logging) {
    super(builder.getName(), builder.getDescription(), builder.getUsage(), Collections.emptyList());

    this.logging = logging;
  }
}
