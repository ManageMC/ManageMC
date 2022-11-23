package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.plugins.bukkit.BukkitWrapper;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.command.util.SimplifiedTabCompleter;
import com.managemc.spigot.command.util.punishments.PunishmentFlags;
import com.managemc.spigot.util.permissions.PermissibleAction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CmdBanMuteTabCompleter implements SimplifiedTabCompleter {

  private final BukkitWrapper bukkitWrapper;
  private final SimplifiedTabCompleter flagsTabCompleter = new FlagsTabCompleter()
      .registerIncompatibility(PunishmentFlags.BROADCAST_FLAG, PunishmentFlags.PRIVATE_FLAG)
      .registerIncompatibility(PunishmentFlags.BROADCAST_FLAG, PunishmentFlags.SHADOW_FLAG)
      .registerIncompatibility(PunishmentFlags.PRIVATE_FLAG, PunishmentFlags.SHADOW_FLAG)
      .registerPermissionRequirement(PunishmentFlags.SHADOW_FLAG, PermissibleAction.SHADOW_PUNISHMENTS);

  public CmdBanMuteTabCompleter(BukkitWrapper bukkitWrapper) {
    this.bukkitWrapper = bukkitWrapper;
  }

  private static final List<String> TAB_COMPLETE_REASON = Collections.singletonList("[reason]");
  private static final List<String> TAB_COMPLETE_DETAILS = Collections.singletonList("[details]");

  @Override
  public List<String> onTabComplete(CommandSender sender, ProcessedCommandArguments args) {
    // if last argument is incomplete and matches any flag
    if (args.lastArgIsIncompleteFlag()) {
      return tabCompleteFlags(sender, args);
    }

    List<String> onlinePlayers = bukkitWrapper.getOnlinePlayers().stream()
        .map(Player::getName)
        .sorted()
        .collect(Collectors.toList());

    switch (args.tabCompletionIndex()) {
      case 0:
        return onlinePlayers.stream()
            .filter(name -> name.toLowerCase().startsWith(args.get(0, "")))
            .collect(Collectors.toList());
      case 1:
        return args.isFinishedTyping() ? TAB_COMPLETE_REASON : tabCompleteFlags(sender, args);
      case 2:
        return args.isFinishedTyping() ? TAB_COMPLETE_DETAILS : tabCompleteFlags(sender, args);
      default:
        return tabCompleteFlags(sender, args);
    }
  }

  private List<String> tabCompleteFlags(CommandSender sender, ProcessedCommandArguments args) {
    return flagsTabCompleter.onTabComplete(sender, args);
  }
}
