package com.managemc.plugins.command;

import lombok.NonNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

abstract class CommandExecutorBase implements CommandExecutor, TabCompleter {

  @NonNull
  @Override
  public final List<String> onTabComplete(
      @NonNull CommandSender sender,
      @NonNull Command command,
      @NonNull String label,
      String @NonNull [] args) {
    return Optional
        .ofNullable(onTabComplete(sender, args))
        .orElse(new ArrayList<>());
  }

  protected abstract List<String> onTabComplete(CommandSender sender, String[] args);
}
