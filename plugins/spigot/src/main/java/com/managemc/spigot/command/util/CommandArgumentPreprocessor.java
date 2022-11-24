package com.managemc.spigot.command.util;

import java.util.*;

public class CommandArgumentPreprocessor {

  private CommandArgumentPreprocessor() {
  }

  public static ProcessedCommandArguments preProcessArguments(Collection<CommandFlag> flags, String[] args) {
    boolean finishedTyping = args.length == 0 || args[args.length - 1].equals("");
    String[] chunkedArgs = processDoubleQuotes(args);
    String[] trimmedArgs = trimSpaces(chunkedArgs);

    Map<String, CommandFlag> knownFlags = new HashMap<>();
    flags.forEach(flag -> {
      knownFlags.put(flag.toString(), flag);
      knownFlags.put(flag.aliasToString(), flag);
    });

    List<String> argsWithoutFlags = new ArrayList<>();
    Map<String, String> processedFlags = new HashMap<>();

    for (String arg : trimmedArgs) {
      if (knownFlags.containsKey(arg)) {
        processedFlags.put(knownFlags.get(arg).getFlag(), "");
      } else if (arg.contains("=") && arg.split("=").length > 0 && knownFlags.containsKey(arg.split("=")[0] + "=")) {
        String[] splitArg = arg.split("=");
        String key = splitArg[0] + "=";
        String value = String.join("", Arrays.copyOfRange(splitArg, 1, splitArg.length));
        processedFlags.put(knownFlags.get(key).getFlag(), value);
      } else {
        argsWithoutFlags.add(arg);
      }
    }

    return ProcessedCommandArguments.builder()
        .finishedTyping(finishedTyping)
        .args(argsWithoutFlags.toArray(new String[0]))
        .flags(processedFlags)
        .expectedFlags(flags)
        .build();
  }

  /**
   * All ManageMC commands interpret multiple words wrapped in quotation marks
   * as a single argument without quotation marks. This allows users to (for
   * example) ban a player with a multi-word reason:
   * /mmc bans create player123 "being annoying" [other args...]
   * <p>
   * For the current implementation:
   * - single quotes are ignored
   * - double quotes in the middles of words will not be chunked
   * - unclosed quotes will not be chunked
   * - quotes not connected to any word may be interpreted as starting or ending quotes
   * - we don't support quotes within quotes (use single quotes within double quotes instead)
   */
  public static String[] processDoubleQuotes(String[] args) {
    List<String> processedArgs = new ArrayList<>();

    List<String> tempChunkedArgs = new ArrayList<>();

    for (String arg : args) {
      if (tempChunkedArgs.size() == 0) {
        // no unprocessed quotes discovered thus far
        if (arg.startsWith("\"")) {
          tempChunkedArgs.add(arg);
        } else {
          processedArgs.add(arg);
        }
      } else {
        // searching for a closing quote
        if (arg.endsWith("\"")) {
          // we found a closing quote, so join the strings together after removing the start/end quotes
          tempChunkedArgs.add(arg);
          String chunkedArg = String.join(" ", tempChunkedArgs);
          processedArgs.add(chunkedArg.substring(1, chunkedArg.length() - 1).trim());
          tempChunkedArgs.clear();
        } else {
          tempChunkedArgs.add(arg);
        }
      }
    }

    if (tempChunkedArgs.size() > 0) {
      // this means there was an opening quote, but we never found a closing one, so we do not attempt to chunk
      processedArgs.addAll(tempChunkedArgs);
    }

    return processedArgs.toArray(new String[0]);
  }

  /**
   * If a player presses space between command words, the spaces will show up as
   * extra arguments in tab completion handlers. Spaces have no programmatic
   * significance to ManageMC except to indicate that a command sender considers
   * their final argument to be complete and that tab completion (if any) should
   * provide advice for the argument immediately following that one. To make it
   * easier for us to process tab completion logic, we remove all "space"
   * arguments and flag whether the final argument was a space.
   */
  private static String[] trimSpaces(String[] args) {
    return Arrays.stream(args).filter(arg -> !arg.equals("")).toArray(String[]::new);
  }
}
