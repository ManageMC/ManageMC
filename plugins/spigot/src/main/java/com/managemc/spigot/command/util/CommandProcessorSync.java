package com.managemc.spigot.command.util;

import com.managemc.plugins.logging.BukkitLogging;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.HashSet;

public abstract class CommandProcessorSync extends CommandProcessorBase {

  public CommandProcessorSync(BukkitLogging logging, String usage) {
    super(logging, usage, new HashSet<>());
  }

  public CommandProcessorSync(BukkitLogging logging, String usage, Collection<CommandFlag> flags) {
    super(logging, usage, flags);
  }

  @Override
  protected final void handle(CommandSender sender, ProcessedCommandArguments args) {
    CommandWrapping.wrap(this::processCommand, sender, logging, usage);
  }

  public abstract void processCommand() throws Exception;
}
