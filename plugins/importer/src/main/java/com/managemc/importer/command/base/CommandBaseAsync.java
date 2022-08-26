package com.managemc.importer.command.base;

import com.managemc.plugins.logging.BukkitLogging;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicLong;

public abstract class CommandBaseAsync extends CommandBase {

  public static final AtomicLong COMPLETED_ASYNC_COMMANDS = new AtomicLong();

  public CommandBaseAsync(CommandBuilder builder, BukkitLogging logging) {
    super(builder, logging);
  }

  @Override
  public final boolean execute(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
    AsyncCommandHandler handler = getNewHandler(sender, args);
    boolean validationSucceeded = CommandWrapping
        .wrap(handler::preProcessCommand, sender, logging, getUsage());
    if (!validationSucceeded) {
      COMPLETED_ASYNC_COMMANDS.addAndGet(1);
      return true;
    }
    new Thread(() -> CommandWrapping.wrap(() -> {
      try {
        handler.processCommandAsync();
      } finally {
        COMPLETED_ASYNC_COMMANDS.addAndGet(1);
      }
    }, sender, logging, getUsage())).start();
    return true;
  }

  public abstract AsyncCommandHandler getNewHandler(CommandSender sender, String[] args);
}
