package com.managemc.plugins.command;

import com.managemc.plugins.logging.BukkitLogging;
import lombok.NonNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Optional;

/**
 * Commands that should be executed immediately with the main server thread
 * should all extend this class. Extend #{@link CommandExecutorAsync} instead
 * to implement commands with HTTP requests and other blocking logic.
 */
public abstract class CommandExecutorSync extends CommandExecutorBase {

  private final BukkitLogging logging;

  public CommandExecutorSync(BukkitLogging logging) {
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
        .wrap(() -> onCommand(sender, args), sender, logging);

    return Optional.ofNullable(result.getError())
        .map(e -> !e.isSyntactic())
        .orElse(true);
  }

  /**
   * Throw #{@link AbortCommand} to abort the command immediately with an error
   * message for the sender.
   */
  protected abstract void onCommand(CommandSender sender, String[] args) throws Exception;
}
