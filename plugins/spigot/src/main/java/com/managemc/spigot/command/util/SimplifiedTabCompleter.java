package com.managemc.spigot.command.util;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface SimplifiedTabCompleter {

  List<String> onTabComplete(CommandSender sender, ProcessedCommandArguments args);
}
