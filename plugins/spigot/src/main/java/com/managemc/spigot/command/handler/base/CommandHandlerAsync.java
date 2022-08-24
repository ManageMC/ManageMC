package com.managemc.spigot.command.handler.base;

import com.managemc.spigot.command.util.ProcessedCommandArguments;
import org.bukkit.command.CommandSender;

public abstract class CommandHandlerAsync {

  protected final CommandSender sender;
  protected final ProcessedCommandArguments args;

  public CommandHandlerAsync(CommandSender sender, ProcessedCommandArguments args) {
    this.sender = sender;
    this.args = args;
  }

  /**
   * Perform synchronous actions, such as command input validation.
   */
  public abstract void preProcessCommand() throws Exception;

  /**
   * Perform any asynchronous operations that cannot block the main server thread. This always runs
   * after preProcessCommand.
   */
  public abstract void processCommandAsync() throws Exception;
}
