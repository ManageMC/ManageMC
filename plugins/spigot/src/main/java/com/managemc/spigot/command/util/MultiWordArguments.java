package com.managemc.spigot.command.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class MultiWordArguments {

  public static String startingAtIndex(String[] args, int index) {
    if (index > args.length - 1 || args.length < index + 1) {
      return null;
    }

    String multiWordArgument = Arrays
        .stream(args, index, args.length)
        .collect(Collectors.joining(" "));

    if (multiWordArgument.length() == 0) {
      return null;
    }

    return multiWordArgument;
  }
}
