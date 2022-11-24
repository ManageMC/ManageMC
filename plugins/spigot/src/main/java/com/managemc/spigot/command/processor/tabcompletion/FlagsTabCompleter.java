package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.spigot.command.util.CommandFlag;
import com.managemc.spigot.util.permissions.PermissibleAction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class FlagsTabCompleter {

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

  public List<String> onTabComplete(Collection<CommandFlag> flags, CommandSender sender, String[] args) {
    Set<String> argsSet = Arrays.stream(args)
        .map(arg -> {
          String[] split = arg.split("=");
          return split[0].equals(arg) ? arg : split[0] + "=";
        })
        .collect(Collectors.toSet());

    Map<String, CommandFlag> flagsByLabel = flags.stream()
        .collect(Collectors.toMap(CommandFlag::getFlag, f -> f));


    List<CommandFlag> filteredFlags = flags.stream()
        .filter((expectedFlag) -> !argsSet.contains(expectedFlag.toString()))
        .filter((expectedFlag) -> !argsSet.contains(expectedFlag.aliasToString()))
        .filter((expectedFlag) -> Optional
            .ofNullable(incompatibilities.get(expectedFlag.getFlag()))
            .orElse(Collections.emptySet())
            .stream().noneMatch(incompat -> {
              CommandFlag flag = flagsByLabel.get(incompat);
              return argsSet.contains(flag.toString()) || argsSet.contains(flag.aliasToString());
            })).collect(Collectors.toList());

    if (sender instanceof Player) {
      filteredFlags = filteredFlags.stream()
          .filter((expectedFlag) -> Optional
              .ofNullable(permissionRequirements.get(expectedFlag.getFlag()))
              .map(action -> action.isAllowed(sender))
              .orElse(true))
          .collect(Collectors.toList());
    }

    String lastArg = args[args.length - 1];

    if (lastArg.equals("")) {
      return filteredFlags.stream().map(CommandFlag::toString).sorted().collect(Collectors.toList());
    }

    return filteredFlags.stream()
        .filter(flag -> flag.toString().startsWith(lastArg) || flag.aliasToString().startsWith(lastArg))
        .map(CommandFlag::toString)
        .sorted()
        .collect(Collectors.toList());
  }
}
