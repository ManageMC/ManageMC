package com.managemc.spigot.command.processor;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.processor.tabcompletion.OnlinePlayersTabCompleter;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.MultiWordArguments;
import com.managemc.spigot.config.SpigotPluginConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class CmdReport extends CommandExecutorAsync {

  public static final String NO_SELF_REPORTS = "You cannot report yourself";
  public static final String SUCCESS_MSG = ChatColor.GREEN + "Thank you for the report. If you have video " +
      "evidence to add, please upload it to YouTube and submit it at https://managemc.com/reports.";
  public static final String OFFLINE_MSG = ChatColor.YELLOW + "When possible, it is best to use this command while " +
      "the player is still online and while you are recording them.";

  private final SpigotPluginConfig config;
  private final OnlinePlayersTabCompleter tabCompleter;

  public CmdReport(SpigotPluginConfig config) {
    super(config.getLogging());
    this.config = config;
    this.tabCompleter = new OnlinePlayersTabCompleter(config.getBukkitWrapper());
  }

  private Player playerSender;
  private String username;
  private String reason;

  @Override
  protected void preProcessCommand(CommandSender sender, String[] args) throws Exception {
    playerSender = CommandAssertions.assertPlayerSender(sender);
    CommandAssertions.assertArgsLengthAtLeast(args, 1);
    CommandAssertions.assertTrue(!sender.getName().equalsIgnoreCase(args[0]), NO_SELF_REPORTS, false);
    username = CommandAssertions.assertValidUsername(args[0]);
    reason = MultiWordArguments.startingAtIndex(args, 1);
  }

  @Override
  protected void onCommandAsync(CommandSender sender) throws Exception {
    Player onlineAccused = config.getBukkitWrapper().getOnlinePlayer(username);
    if (onlineAccused != null) {
      config.getReportsService().reportOnlinePlayer(playerSender, onlineAccused, reason);
    } else {
      UUID uuid = CommandAssertions.usernameToUuidBlocking(config.getUuidResolver(), username);
      config.getReportsService().reportOfflinePlayer(playerSender, uuid, reason);
      sender.sendMessage(OFFLINE_MSG);
    }

    sender.sendMessage(SUCCESS_MSG);
  }

  @Override
  protected List<String> onTabComplete(CommandSender sender, String[] args) {
    return tabCompleter.onTabComplete(args);
  }
}
