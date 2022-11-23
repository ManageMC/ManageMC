package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.plugins.bukkit.BukkitWrapper;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.command.util.SimplifiedTabCompleter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CmdWarnTabCompleter implements SimplifiedTabCompleter {

  private static final List<String> TAB_COMPLETE_REASON = Collections.singletonList("[reason]");
  private static final List<String> TAB_COMPLETE_DETAILS = Collections.singletonList("[details]");

  private final BukkitWrapper bukkitWrapper;

  public CmdWarnTabCompleter(BukkitWrapper bukkitWrapper) {
    this.bukkitWrapper = bukkitWrapper;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, ProcessedCommandArguments args) {
    switch (args.tabCompletionIndex()) {
      case 0:
        return getMatchingOnlinePlayers(args.get(0, ""));
      case 1:
        return args.isFinishedTyping() ? TAB_COMPLETE_REASON : new ArrayList<>();
      case 2:
        return args.isFinishedTyping() ? TAB_COMPLETE_DETAILS : new ArrayList<>();
      default:
        return new ArrayList<>();
    }
  }

  private List<String> getMatchingOnlinePlayers(String lastArg) {
    return bukkitWrapper.getOnlinePlayers().stream()
        .map(Player::getName)
        .filter(name -> name.toLowerCase().startsWith(lastArg.toLowerCase()))
        .sorted()
        .collect(Collectors.toList());
  }
}
