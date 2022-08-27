package com.managemc.importer.command.base;

public class CommandValidationException extends IllegalArgumentException {

  private final boolean syntactic;

  public CommandValidationException(String message, boolean syntactic) {
    super(message);
    this.syntactic = syntactic;
  }

  boolean isSyntactic() {
    return syntactic;
  }
}