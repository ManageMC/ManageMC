package com.managemc.spigot.command.util;

import lombok.Builder;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

@Builder
public class ProcessedCommandArguments {
  @Getter
  private final String[] args;
  @Getter
  private boolean finishedTyping;
  @Getter
  private final Map<String, String> flags;
  @Getter
  private final Collection<CommandFlag> expectedFlags;

  public static ProcessedCommandArguments forSubHandler(
      ProcessedCommandArguments oldArgs,
      CommandProcessorBase processor,
      int currentIndex
  ) {
    ProcessedCommandArguments newArgs = CommandArgumentPreprocessor.preProcessArguments(
        processor.flags,
        Arrays.copyOfRange(oldArgs.getArgs(), currentIndex + 1, oldArgs.getArgs().length)
    );
    newArgs.finishedTyping = oldArgs.finishedTyping;
    return newArgs;
  }

  public String get(int index) {
    return get(index, null);
  }

  public String get(int index, String orElse) {
    return args.length > index ? args[index] : orElse;
  }

  public String lastArg() {
    return args.length == 0 ? null : args[args.length - 1];
  }

  public int length() {
    return args.length;
  }

  public boolean hasFlag(String flag) {
    return flags.get(flag) != null;
  }

  public String getFlag(String flag) {
    return flags.get(flag);
  }

  public boolean lastArgIsIncompleteFlag() {
    if (finishedTyping) {
      return false;
    }

    String lastArg = lastArg();

    return expectedFlags.stream()
        .anyMatch(flag -> flag.toString().startsWith(lastArg) || flag.aliasToString().startsWith(lastArg));
  }

  /**
   * This refers to the index of the term that requires tab completion. Tab completion
   * handlers will typically switch on this value to avoid if/else logic around the
   * presence of a trailing space.
   */
  public int tabCompletionIndex() {
    return Math.max(0, finishedTyping ? args.length : args.length - 1);
  }
}
