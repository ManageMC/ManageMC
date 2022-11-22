package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.plugins.bukkit.BukkitWrapper;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.command.util.SimplifiedTabCompleter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerTabCompleter implements SimplifiedTabCompleter {

  private final BukkitWrapper bukkitWrapper;

  public PlayerTabCompleter(BukkitWrapper bukkitWrapper) {
    this.bukkitWrapper = bukkitWrapper;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, ProcessedCommandArguments args) {
    if (args.tabCompletionIndex() == 0) {
      return getMatchingOnlinePlayers(args.get(0, ""));
    }
    return new ArrayList<>();
  }

  private List<String> getMatchingOnlinePlayers(String lastArg) {
    return bukkitWrapper.getOnlinePlayers().stream()
        .map(Player::getName)
        .filter(name -> name.toLowerCase().startsWith(lastArg.toLowerCase()))
        .sorted()
        .collect(Collectors.toList());
  }
}
