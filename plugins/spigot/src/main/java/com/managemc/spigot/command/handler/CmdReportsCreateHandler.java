package com.managemc.spigot.command.handler;

import com.managemc.spigot.command.handler.base.CommandHandlerAsync;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.config.SpigotPluginConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

public class CmdReportsCreateHandler extends CommandHandlerAsync {

  public static final String NO_SELF_REPORTS = "You cannot report yourself";
  public static final String SUCCESS_MSG = ChatColor.GREEN + "Thank you for the report. If you have video " +
      "evidence to add, please upload it to YouTube and submit it at https://managemc.com/reports.";
  public static final String OFFLINE_MSG = ChatColor.YELLOW + "When possible, it is best to use this command while " +
      "the player is still online and while you are recording them.";

  private final SpigotPluginConfig config;

  public CmdReportsCreateHandler(CommandSender sender, ProcessedCommandArguments args, SpigotPluginConfig config) {
    super(sender, args);
    this.config = config;
  }

  @Override
  public void preProcessCommand() throws Exception {
    playerSender = CommandAssertions.assertPlayerSender(sender);
    CommandAssertions.assertArgsLengthAtLeast(args.getArgs(), 1);
    CommandAssertions.assertTrue(!sender.getName().equalsIgnoreCase(args.get(0)), NO_SELF_REPORTS, false);
    CommandAssertions.assertValidUsername(args.get(0));
  }

  private Player playerSender;

  @Override
  public void processCommandAsync() throws Exception {
    Player onlineAccused = config.getBukkitWrapper().getOnlinePlayer(args.get(0));
    if (onlineAccused != null) {
      config.getReportsService().reportOnlinePlayer(playerSender, onlineAccused, determineReason());
    } else {
      UUID uuid = CommandAssertions.usernameToUuidBlocking(config.getUuidResolver(), args.get(0));
      config.getReportsService().reportOfflinePlayer(playerSender, uuid, determineReason());
      sender.sendMessage(OFFLINE_MSG);
    }

    sender.sendMessage(SUCCESS_MSG);
  }

  private String determineReason() {
    if (args.length() < 2) {
      return null;
    }

    String reason = Arrays
        .stream(args.getArgs(), 1, args.getArgs().length)
        .collect(Collectors.joining(" "));

    if (reason.length() == 0) {
      return null;
    }

    return reason;
  }
}
