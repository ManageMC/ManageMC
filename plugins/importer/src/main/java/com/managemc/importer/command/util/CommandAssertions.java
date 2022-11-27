package com.managemc.importer.command.util;

import com.managemc.plugins.command.AbortCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandAssertions {

  public static final String CONSOLE_SENDER_ONLY = "This command must be run from the console";
  public static String WRONG_NUM_ARGS = "Wrong number of arguments";

  public static void assertConsoleSender(CommandSender sender) {
    if (!(sender instanceof ConsoleCommandSender)) {
      throw AbortCommand.withoutUsageMessage(CONSOLE_SENDER_ONLY);
    }
  }

  public static void assertTrue(boolean expression, String errorMessage, boolean syntactic) {
    if (!expression) {
      if (syntactic) {
        throw AbortCommand.withUsageMessage(errorMessage);
      }
      throw AbortCommand.withoutUsageMessage(errorMessage);
    }
  }

  public static void assertArgsLength(String[] args, int length) {
    assertTrue(args.length == length, WRONG_NUM_ARGS, true);
  }

  public static void assertArgsLengthAtLeast(String[] args, int atLeast) {
    assertTrue(args.length >= atLeast, WRONG_NUM_ARGS, true);
  }

  public static int assertInteger(String argument) {
    try {
      int actual = Integer.parseInt(argument);
      if (String.valueOf(actual).equals(argument)) {
        return actual;
      }
    } catch (NumberFormatException ignored) {
    }
    throw AbortCommand.withUsageMessage("Invalid integer " + argument);
  }

  public static <T extends Enum<T>> T assertOneOf(String argument, Class<T> clazz) {
    List<T> values = Arrays.stream(clazz.getEnumConstants())
        .filter(value -> !value.toString().equals("UNRECOGNIZED"))
        .collect(Collectors.toList());

    for (T value : values) {
      if (value.toString().equals(argument.toUpperCase())) {
        return value;
      }
    }

    String message = "Invalid argument '" + argument + "'. Must be one of: " + values;
    throw AbortCommand.withUsageMessage(message);
  }
}
