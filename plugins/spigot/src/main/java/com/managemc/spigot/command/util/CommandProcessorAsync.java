package com.managemc.spigot.command.util;

import com.managemc.plugins.logging.BukkitLogging;
import com.managemc.spigot.command.handler.base.CommandHandlerAsync;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class CommandProcessorAsync extends CommandProcessorBase {

  public static final AtomicInteger COMPLETED_ASYNC_COMMANDS = new AtomicInteger(0);

  private final SimplifiedTabCompleter tabCompleter;

  public CommandProcessorAsync(
      BukkitLogging logging,
      String usage,
      SimplifiedTabCompleter tabCompleter
  ) {
    this(logging, usage, tabCompleter, new HashSet<>());
  }

  public CommandProcessorAsync(
      BukkitLogging logging,
      String usage,
      SimplifiedTabCompleter tabCompleter,
      Collection<CommandFlag> flags
  ) {
    super(logging, usage, flags);
    this.tabCompleter = tabCompleter;
  }

  @Override
  protected final void handle(CommandSender sender, ProcessedCommandArguments args) {
    CommandHandlerAsync handler = getNewHandler(sender, args);
    boolean validationSucceeded = CommandWrapping
        .wrap(handler::preProcessCommand, sender, logging, usage);
    if (!validationSucceeded) {
      COMPLETED_ASYNC_COMMANDS.addAndGet(1);
      return;
    }
    new Thread(() -> CommandWrapping.wrap(() -> {
      try {
        handler.processCommandAsync();
      } finally {
        COMPLETED_ASYNC_COMMANDS.addAndGet(1);
      }
    }, sender, logging, usage)).start();
  }

  @Override
  public final List<String> onTabComplete(CommandSender sender, ProcessedCommandArguments args) {
    return tabCompleter == null ? new ArrayList<>() : tabCompleter.onTabComplete(sender, args);
  }

  public abstract CommandHandlerAsync getNewHandler(CommandSender sender, ProcessedCommandArguments args);
}
