package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.spigot.command.handler.CmdPunishmentsPardonHandler;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.command.util.SimplifiedTabCompleter;
import com.managemc.spigot.state.PardonablePunishmentData;
import com.managemc.spigot.util.permissions.PermissibleAction;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CmdPunishmentsPardonTabCompleter implements SimplifiedTabCompleter {

  // keep these in alphabetical order
  private static final String[] INCOMPLETE_IDS = new String[]{"ban-", "ip_ban-", "mute-", "warning-"};

  private final PardonablePunishmentData punishmentData;

  public CmdPunishmentsPardonTabCompleter(PardonablePunishmentData punishmentData) {
    this.punishmentData = punishmentData;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, ProcessedCommandArguments args) {
    if (PermissibleAction.PARDON_POSSIBLY.isAllowed(sender)) {
      List<String> tabComplete = onTabCompleteDisregardingPermissions(sender, args);

      destructivelyFilterTermsByPermission(tabComplete, sender, "ban", PermissibleAction.BAN_PLAYER);
      destructivelyFilterTermsByPermission(tabComplete, sender, "ip_ban", PermissibleAction.IP_BAN_PLAYER);
      destructivelyFilterTermsByPermission(tabComplete, sender, "mute", PermissibleAction.MUTE_PLAYER);
      destructivelyFilterTermsByPermission(tabComplete, sender, "warning", PermissibleAction.WARN_PLAYER);

      return tabComplete;
    }

    return new ArrayList<>();
  }

  private void destructivelyFilterTermsByPermission(
      List<String> tabComplete,
      CommandSender sender,
      String prefix,
      PermissibleAction action
  ) {
    if (!action.isAllowed(sender)) {
      tabComplete.removeIf(term -> term.toLowerCase().startsWith(prefix));
    }
  }

  private List<String> onTabCompleteDisregardingPermissions(CommandSender sender, ProcessedCommandArguments args) {
    List<String> defaultOptions = defaultOptions(sender);

    Set<String> argsSet = new HashSet<>(Arrays.asList(args.getArgs()));
    List<String> allOptionsFiltered = defaultOptions.stream()
        .filter(arg -> !argsSet.contains(arg.toLowerCase()))
        .collect(Collectors.toList());

    if (args.length() == 0) {
      // user hasn't typed anything
      return allOptionsFiltered;
    }

    String lastArg = args.lastArg().toLowerCase();

    Integer firstNonMatchingArgIndex = firstNonMatchingArgIndex(args.getArgs());
    if (firstNonMatchingArgIndex == null) {
      if (args.isFinishedTyping()) {
        return allOptionsFiltered;
      } else {
        // all user's args are syntactically valid IDs, but the user hasn't pressed space yet
        if (defaultOptions.contains(lastArg)) {
          // last argument is a known ID or default tab-complete option
          return Collections.singletonList(lastArg);
        }
        // last arg is syntactically valid but didn't exactly match one of the suggested options
        return allOptionsFiltered.stream()
            .filter(option -> option.startsWith(lastArg))
            .collect(Collectors.toList());
      }
    }

    if (firstNonMatchingArgIndex == args.tabCompletionIndex()) {
      if (args.isFinishedTyping()) {
        // user pressed space and all other args are valid
        return allOptionsFiltered;
      }

      // user hasn't finished typing their last argument
      List<String> matchingOptions = allOptionsFiltered.stream()
          .filter(option -> option.startsWith(lastArg))
          .collect(Collectors.toList());

      if (matchingOptions.size() == 0) {
        String matchingDefault = Stream.of(INCOMPLETE_IDS)
            .filter(id -> id.startsWith(lastArg)).findFirst().orElse(null);
        if (matchingDefault == null) {
          return new ArrayList<>();
        }
        return Collections.singletonList(matchingDefault);
      }

      return matchingOptions;
    }

    return new ArrayList<>();
  }

  private Integer firstNonMatchingArgIndex(String[] args) {
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if (!arg.toUpperCase().matches(CmdPunishmentsPardonHandler.ID_REGEX)) {
        // allow partial completions (e.g. "ban-"), but only in the final argument
        if (i == args.length - 1 && new HashSet<>(Arrays.asList(INCOMPLETE_IDS)).contains(arg.toLowerCase())) {
          return null;
        }
        return i;
      }
    }
    return null;
  }

  private List<String> defaultOptions(CommandSender sender) {
    List<String> allOptions = punishmentData.getPunishmentIdsKnownToSender(sender);

    if (allOptions.size() > 0) {
      return allOptions;
    }

    return Arrays.asList(INCOMPLETE_IDS);
  }
}
