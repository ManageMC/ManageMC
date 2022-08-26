package com.managemc.spigot.command.processor;

import com.managemc.spigot.command.util.CommandNodeMap;
import com.managemc.spigot.command.util.CommandValidationException;
import com.managemc.spigot.command.util.CommandWrapping;
import com.managemc.spigot.config.SpigotPluginConfig;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class CmdMMC implements CommandExecutor {

  private final SpigotPluginConfig config;
  private final CommandNodeMap commandNodes;

  public CmdMMC(SpigotPluginConfig config) {
    this.config = config;
    this.commandNodes = new CommandNodeMap(config);
  }

  @Override
  public boolean onCommand(
      @NonNull CommandSender sender,
      @NonNull Command command,
      @NonNull String s,
      String @NonNull [] args
  ) {
    try {
      onCommand(sender, args);
    } catch (RuntimeException e) {
      sender.sendMessage(CommandWrapping.HARD_FAILURE_MESSAGE);
      config.getLogging().logStackTrace(e);
    }

    // Normally this returns whether the command succeeded, and if it failed, the usage
    // message is displayed to the CommandSender. We prefer not to do that, because from
    // Bukkit's perspective, this plugin only has one command, so its usage will not be
    // very helpful.
    return true;
  }

  void onCommand(@NonNull CommandSender sender, String @NonNull [] args) {
    CommandNodeMap.ProcessorNode node;
    try {
      node = commandNodes.searchForHandler(sender, args);
    } catch (CommandValidationException e) {
      sender.sendMessage(ChatColor.RED + e.getMessage());
      return;
    }

    String[] argsAfterCommandKeyword = Arrays.copyOfRange(args, node.getArgIndex() + 1, args.length);
    node.getProcessor().process(sender, argsAfterCommandKeyword);
  }
}
