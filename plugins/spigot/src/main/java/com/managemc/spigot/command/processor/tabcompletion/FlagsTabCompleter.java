package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.spigot.command.util.CommandFlag;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.command.util.SimplifiedTabCompleter;
import com.managemc.spigot.util.permissions.PermissibleAction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class FlagsTabCompleter implements SimplifiedTabCompleter {

  private final Map<String, Set<String>> incompatibilities = new HashMap<>();
  private final Map<String, PermissibleAction> permissionRequirements = new HashMap<>();

  public FlagsTabCompleter registerIncompatibility(String flag1, String flag2) {
    incompatibilities.computeIfAbsent(flag1, (key) -> new HashSet<>());
    incompatibilities.computeIfAbsent(flag2, (key) -> new HashSet<>());

    incompatibilities.get(flag1).add(flag2);
    incompatibilities.get(flag2).add(flag1);

    return this;
  }

  public FlagsTabCompleter registerPermissionRequirement(String flag, PermissibleAction action) {
    permissionRequirements.put(flag, action);

    return this;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, ProcessedCommandArguments args) {
    List<CommandFlag> filteredFlags = args.getExpectedFlags().stream()
        .filter((expectedFlag) -> !args.hasFlag(expectedFlag.getFlag()))
        .filter((expectedFlag) -> Optional
            .ofNullable(incompatibilities.get(expectedFlag.getFlag()))
            .orElse(Collections.emptySet())
            .stream().noneMatch(args::hasFlag))
        .collect(Collectors.toList());

    if (sender instanceof Player) {
      filteredFlags = filteredFlags.stream()
          .filter((expectedFlag) -> Optional
              .ofNullable(permissionRequirements.get(expectedFlag.getFlag()))
              .map(action -> action.isAllowed(sender))
              .orElse(true))
          .collect(Collectors.toList());
    }

    if (args.isFinishedTyping()) {
      return filteredFlags.stream().map(CommandFlag::toString).sorted().collect(Collectors.toList());
    }

    String lastArg = args.lastArg();

    return filteredFlags.stream()
        .filter(flag -> flag.toString().startsWith(lastArg) || flag.aliasToString().startsWith(lastArg))
        .map(CommandFlag::toString)
        .sorted()
        .collect(Collectors.toList());
  }
}
