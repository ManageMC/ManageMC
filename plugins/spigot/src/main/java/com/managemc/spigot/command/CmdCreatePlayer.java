package com.managemc.spigot.command;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.config.SpigotPluginConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

public class CmdCreatePlayer extends CommandExecutorAsync {

  public static final String SUCCESS_MSG = ChatColor.GREEN + "Successfully added %s to the ManageMC database";

  private final SpigotPluginConfig config;

  /**
   * Specifically do NOT tab complete anything. All online players exist already in our db unless there
   * was a runtime error, so the user is probably looking for an offline user which we have no knowledge
   * about.
   */
  public CmdCreatePlayer(SpigotPluginConfig config) {
    super(config.getLogging());
    this.config = config;
  }

  private String username;

  @Override
  protected void preProcessCommand(CommandSender sender, String[] args) throws Exception {
    CommandAssertions.assertArgsLength(args, 1);
    username = CommandAssertions.assertValidUsername(args[0]);
  }

  @Override
  protected void onCommandAsync(CommandSender sender) throws Exception {
    UUID uuid = CommandAssertions.usernameToUuidBlocking(config.getUuidResolver(), username);

    if (uuid != null) {
      config.getCreatePlayerService().createPlayer(username, uuid);
    }

    sender.sendMessage(String.format(SUCCESS_MSG, username));
  }

  @Override
  protected List<String> onTabComplete(CommandSender sender, String[] args) {
    return null;
  }
}
