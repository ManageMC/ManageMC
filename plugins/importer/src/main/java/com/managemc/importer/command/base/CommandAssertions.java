package com.managemc.importer.command.base;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandAssertions {

  public static String WRONG_NUM_ARGS = "Wrong number of arguments";

  public static void assertTrue(boolean expression, String errorMessage, boolean syntactic) {
    if (!expression) {
      throw new CommandValidationException(errorMessage, syntactic);
    }
  }

  public static void assertArgsLength(String[] args, int length) throws CommandValidationException {
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
    throw new CommandValidationException("Invalid integer " + argument, true);
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
    throw new CommandValidationException(message, true);
  }
}
