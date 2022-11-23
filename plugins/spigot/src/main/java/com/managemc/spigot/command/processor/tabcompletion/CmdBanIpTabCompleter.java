package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.command.util.SimplifiedTabCompleter;
import com.managemc.spigot.command.util.punishments.PunishmentFlags;
import com.managemc.spigot.util.permissions.PermissibleAction;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdBanIpTabCompleter implements SimplifiedTabCompleter {

  private static final List<String> TAB_COMPLETE_IP = Collections.singletonList("<ip/range>");
  private static final List<String> TAB_COMPLETE_REASON = Collections.singletonList("[reason]");
  private static final List<String> TAB_COMPLETE_DETAILS = Collections.singletonList("[details]");

  private final SimplifiedTabCompleter flagsTabCompleter = new FlagsTabCompleter()
      .registerIncompatibility(PunishmentFlags.BROADCAST_FLAG, PunishmentFlags.PRIVATE_FLAG)
      .registerIncompatibility(PunishmentFlags.BROADCAST_FLAG, PunishmentFlags.SHADOW_FLAG)
      .registerIncompatibility(PunishmentFlags.PRIVATE_FLAG, PunishmentFlags.SHADOW_FLAG)
      .registerPermissionRequirement(PunishmentFlags.SHADOW_FLAG, PermissibleAction.SHADOW_PUNISHMENTS);

  @Override
  public List<String> onTabComplete(CommandSender sender, ProcessedCommandArguments args) {
    // if last argument is incomplete and matches any flag
    if (args.lastArgIsIncompleteFlag()) {
      return tabCompleteFlags(sender, args);
    }

    if (args.isFinishedTyping()) {
      switch (args.tabCompletionIndex()) {
        case 0:
          return TAB_COMPLETE_IP;
        case 1:
          return TAB_COMPLETE_REASON;
        case 2:
          return TAB_COMPLETE_DETAILS;
        default:
          return tabCompleteFlags(sender, args);
      }
    }

    return tabCompleteFlags(sender, args);
  }

  private List<String> tabCompleteFlags(CommandSender sender, ProcessedCommandArguments args) {
    return flagsTabCompleter.onTabComplete(sender, args);
  }
}
