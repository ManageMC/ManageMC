package com.managemc.spigot.command;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.util.CommandArgumentPreprocessor;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.util.permissions.PermissibleAction;
import org.bukkit.command.CommandSender;
import org.openapitools.client.model.PunishmentIdAndType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CmdPardon extends CommandExecutorAsync {

  public static final String ID_REGEX;
  public static final String BAD_ID_MESSAGE = "One or more invalid punishment IDs detected (correct example: ban-123)";
  public static final String NO_BAN_MSG = "You cannot pardon bans";
  public static final String NO_MUTE_MSG = "You cannot pardon mutes";
  public static final String NO_WARNING_MSG = "You cannot pardon warnings";
  public static final String NO_IP_BAN_MSG = "You cannot pardon IP bans";

  // keep these in alphabetical order
  private static final String[] INCOMPLETE_IDS = new String[]{"ban-", "ip_ban-", "mute-", "warning-"};

  static {
    List<String> typeOptions = Arrays.stream(PunishmentIdAndType.TypeEnum.values()).map(Enum::name).collect(Collectors.toList());
    ID_REGEX = "^(" + String.join("|", typeOptions) + ")-\\d+$";
  }


  private final SpigotPluginConfig config;

  public CmdPardon(SpigotPluginConfig config) {
    super(config.getLogging());
    this.config = config;
  }

  private Collection<String> punishmentIds;
  private String details;

  /**
   * Special note here: the "edit-any" permission (managemc.players.punishments.edit)
   * allows a user to pardon punishments issued by other players. All users can pardon
   * their own punishments, provided they still have permission to issue the given
   * punishment type, but since this plugin has no way of knowing which player issued a
   * punishment without making API requests, we do not check the "edit-any" permission
   * client-side. If the server throws a 403 when we try to pardon, then we display a permission
   * error to the command sender asynchronously. We do this for every player-initiated API
   * request, because it's always possible for permissions to be out of sync.
   */
  @Override
  protected void preProcessCommand(CommandSender sender, String[] args) throws Exception {
    CommandAssertions.checkPermissions(PermissibleAction.PARDON_POSSIBLY, sender);
    CommandAssertions.assertArgsLengthAtLeast(args, 1);
    List<String> allDetails = new ArrayList<>();

    punishmentIds = new HashSet<>();
    for (String arg : args) {
      if (allDetails.size() > 0) {
        allDetails.add(arg);
      } else if (arg.toUpperCase().matches(ID_REGEX)) {
        punishmentIds.add(arg);
      } else {
        allDetails.add(arg);
      }
    }

    CommandAssertions.assertTrue(punishmentIds.size() > 0, BAD_ID_MESSAGE, true);

    Set<PunishmentIdAndType.TypeEnum> allTypesRequested = new HashSet<>();
    punishmentIds.forEach(id -> {
      String type = id.split("-")[0].toUpperCase();
      allTypesRequested.add(PunishmentIdAndType.TypeEnum.fromValue(type));
    });

    allTypesRequested.forEach(type -> {
      switch (type) {
        case BAN:
          CommandAssertions.checkPermissions(PermissibleAction.BAN_PLAYER, sender, NO_BAN_MSG);
          break;
        case MUTE:
          CommandAssertions.checkPermissions(PermissibleAction.MUTE_PLAYER, sender, NO_MUTE_MSG);
          break;
        case WARNING:
          CommandAssertions.checkPermissions(PermissibleAction.WARN_PLAYER, sender, NO_WARNING_MSG);
          break;
        case IP_BAN:
        case RANGE_IP_BAN:
          CommandAssertions.checkPermissions(PermissibleAction.IP_BAN_PLAYER, sender, NO_IP_BAN_MSG);
          break;
      }
    });

    details = allDetails.size() == 0 ? null : String.join(" ", allDetails);
  }

  @Override
  protected void onCommandAsync(CommandSender sender) throws Exception {
    config.getPardonService().issuePardon(punishmentIds, details, sender);
  }

  @Override
  protected List<String> onTabComplete(CommandSender sender, String[] args) {
    if (PermissibleAction.PARDON_POSSIBLY.isAllowed(sender)) {
      ProcessedCommandArguments processedArgs = CommandArgumentPreprocessor
          .preProcessArguments(new HashSet<>(), args);
      List<String> tabComplete = onTabCompleteDisregardingPermissions(sender, processedArgs);

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
      if (!arg.toUpperCase().matches(ID_REGEX)) {
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
    List<String> allOptions = config.getPardonablePunishmentData().getPunishmentIdsKnownToSender(sender);

    if (allOptions.size() > 0) {
      return allOptions;
    }

    return Arrays.asList(INCOMPLETE_IDS);
  }
}
