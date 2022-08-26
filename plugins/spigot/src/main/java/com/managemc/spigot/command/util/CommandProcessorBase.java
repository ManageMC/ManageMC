package com.managemc.spigot.command.util;

import com.managemc.plugins.logging.BukkitLogging;
import org.bukkit.command.CommandSender;

import java.util.Collection;

public abstract class CommandProcessorBase implements SimplifiedTabCompleter {

  protected final BukkitLogging logging;
  protected final String usage;
  protected final Collection<CommandFlag> flags;

  public CommandProcessorBase(BukkitLogging logging, String usage, Collection<CommandFlag> flags) {
    this.logging = logging;
    this.usage = usage;
    this.flags = flags;
  }

  public void process(CommandSender sender, String[] args) {
    handle(sender, CommandArgumentPreprocessor.preProcessArguments(flags, args));
  }

  protected abstract void handle(CommandSender sender, ProcessedCommandArguments args);
}
