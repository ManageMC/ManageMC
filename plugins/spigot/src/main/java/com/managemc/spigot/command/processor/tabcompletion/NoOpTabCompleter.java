package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.command.util.SimplifiedTabCompleter;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class NoOpTabCompleter implements SimplifiedTabCompleter {

  private static final List<String> NOPE = new ArrayList<>();

  @Override
  public List<String> onTabComplete(CommandSender sender, ProcessedCommandArguments args) {
    return NOPE;
  }
}
