package com.managemc.importer.command.base;

import org.bukkit.command.CommandSender;

public abstract class AsyncCommandHandler {

  protected final CommandSender sender;
  protected final String[] args;

  public AsyncCommandHandler(CommandSender sender, String[] args) {
    this.sender = sender;
    this.args = args;
  }

  /**
   * perform synchronous actions, such as command input validation
   */
  public abstract void preProcessCommand() throws Exception;

  /**
   * perform any asynchronous operations (always runs after preliminaryBehaviorSync)
   */
  public abstract void processCommandAsync() throws Exception;
}
