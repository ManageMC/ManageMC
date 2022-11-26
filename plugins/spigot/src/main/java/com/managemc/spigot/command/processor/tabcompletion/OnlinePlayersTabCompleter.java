package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.plugins.bukkit.BukkitWrapper;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class OnlinePlayersTabCompleter {

  private final BukkitWrapper bukkitWrapper;

  public OnlinePlayersTabCompleter(BukkitWrapper bukkitWrapper) {
    this.bukkitWrapper = bukkitWrapper;
  }

  public List<String> onTabComplete(String[] args) {
    switch (args.length) {
      case 0:
        return getMatchingOnlinePlayers("");
      case 1:
        return getMatchingOnlinePlayers(args[0]);
      default:
        return null;
    }
  }

  private List<String> getMatchingOnlinePlayers(String lastArg) {
    return bukkitWrapper.getOnlinePlayers().stream()
        .map(Player::getName)
        .filter(name -> name.toLowerCase().contains(lastArg.toLowerCase()))
        .sorted(String.CASE_INSENSITIVE_ORDER)
        .collect(Collectors.toList());
  }
}
