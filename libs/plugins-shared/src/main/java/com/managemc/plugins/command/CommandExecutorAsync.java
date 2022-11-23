package com.managemc.plugins.command;

import com.managemc.plugins.logging.BukkitLogging;
import lombok.NonNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Extend this class to implement commands that have asynchronous logic that
 * would block the main server thread. Use this as an alternative to
 * #{@link CommandExecutorSync}.
 */
public abstract class CommandExecutorAsync extends CommandExecutorBase {

  public static final AtomicLong COMPLETED_COMMANDS = new AtomicLong();

  private final BukkitLogging logging;

  public CommandExecutorAsync(BukkitLogging logging) {
    this.logging = logging;
  }

  @Override
  public final boolean onCommand(
      @NonNull CommandSender sender,
      @NonNull Command command,
      @NonNull String label,
      String @NonNull [] args
  ) {
    CommandWrapping.Result result = CommandWrapping
        .wrap(() -> preProcessCommand(sender, args), sender, logging);

    if (!result.isSuccess()) {
      COMPLETED_COMMANDS.addAndGet(1);
      return Optional.ofNullable(result.getError())
          .map(e -> !e.isSyntactic())
          .orElse(true);
    }

    new Thread(() -> CommandWrapping.wrap(() -> {
      try {
        onCommandAsync(sender);
      } finally {
        COMPLETED_COMMANDS.addAndGet(1);
      }
    }, sender, logging)).start();

    return true;
  }

  /**
   * Most command validations belong in this method. Throw #{@link AbortCommand}
   * to abort the command immediately and prevent progression to the async stage.
   * This method runs inside the main server thread.
   */
  protected abstract void preProcessCommand(CommandSender sender, String[] args) throws Exception;

  /**
   * This method will run outside the main server thread, so put all HTTP requests
   * and other blocking operations here.
   */
  protected abstract void onCommandAsync(CommandSender sender) throws Exception;
}
