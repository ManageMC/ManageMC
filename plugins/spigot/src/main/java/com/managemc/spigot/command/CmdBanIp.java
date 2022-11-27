package com.managemc.spigot.command;

import com.managemc.plugins.command.AbortCommand;
import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.tabcompletion.PlayerPunishmentTabCompleter;
import com.managemc.spigot.command.util.CommandArgumentPreprocessor;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.command.util.punishments.PunishmentFlags;
import com.managemc.spigot.command.util.punishments.PunishmentMessageSender;
import com.managemc.spigot.command.util.validation.IpRangeWrapper;
import com.managemc.spigot.command.util.validation.IpV4Wrapper;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.util.KickMessages;
import com.managemc.spigot.util.permissions.PermissibleAction;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.openapitools.client.model.IpBan;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CmdBanIp extends CommandExecutorAsync {

  public static final String OFFLINE_MSG = "This player has not been online recently enough for Bukkit to remember its IP address";
  public static final String NO_IP_MSG = "Bukkit does not know this player's IP address";

  private final SpigotPluginConfig config;
  private final PlayerPunishmentTabCompleter tabCompleter;

  public CmdBanIp(SpigotPluginConfig config) {
    super(config.getLogging());
    this.config = config;
    this.tabCompleter = new PlayerPunishmentTabCompleter(config.getBukkitWrapper());
  }

  private PunishmentFlags flags;
  private String ipAddress;
  private PunishmentMessageSender messageSender;

  @Override
  protected void preProcessCommand(CommandSender sender, String[] args) throws Exception {
    CommandAssertions.checkPermissions(PermissibleAction.IP_BAN_PLAYER, sender);
    CommandAssertions.assertArgsLengthAtLeast(args, 1);
    ProcessedCommandArguments processedArgs = CommandArgumentPreprocessor
        .preProcessArguments(PunishmentFlags.FLAGS, args);
    flags = new PunishmentFlags(processedArgs);
    CommandAssertions.checkPermissionsForPunishmentFlags(flags, sender);

    ipAddress = CommandAssertions.assertValidIpOrIpRange(determineIpAddress(args[0]));

    messageSender = PunishmentMessageSender
        .forIpBans(sender, config.getBukkitWrapper(), config.getServerGroupsData(), flags, args[0]);

    messageSender.handleFlagMisuseWarnings();

    Collection<Player> affectedPlayers = getAffectedPlayers(ipAddress);
    if (affectedPlayers.size() > 1) {
      sender.sendMessage(ChatColor.YELLOW + "More than one online player will be affected by this IP ban");
    }

    affectedPlayers.forEach(player -> {
      if (player.isOnline()) {
        player.kickPlayer(KickMessages.ipBanMessage(flags.toIpBanInput(config.getLocalConfig())));
      }
      messageSender.handlePublicMessage(player.getName());
    });
  }

  @Override
  protected void onCommandAsync(CommandSender sender) throws Exception {
    IpBan ipBan = config.getIpBanService().banIpAddress(sender, ipAddress, flags);

    if (ipBan != null) {
      if (ipBan.getScope().size() > 0) {
        config.getServerGroupsData().refreshBlockingIfNeeded();
      }
      messageSender.sendIssuerMessage(ipBan);
    }
  }

  @Override
  protected List<String> onTabComplete(CommandSender sender, String[] args) {
    if (PermissibleAction.IP_BAN_PLAYER.isAllowed(sender)) {
      return tabCompleter.onTabComplete(sender, args);
    }
    return null;
  }

  private String determineIpAddress(String firstArg) {
    if (firstArg.contains(".")) {
      return firstArg;
    }

    CommandAssertions.assertValidUsername(firstArg);
    Player offender = config.getBukkitWrapper().getOnlineOrRecentlyOnlinePlayer(firstArg);
    if (offender == null) {
      throw AbortCommand.withoutUsageMessage(OFFLINE_MSG);
    }
    if (offender.getAddress() == null) {
      throw AbortCommand.withoutUsageMessage(NO_IP_MSG);
    }

    return offender.getAddress().getAddress().getHostAddress();
  }

  private Collection<Player> getAffectedPlayers(String ipAddress) {
    if (ipAddress.contains("-")) {
      return getAffectedPlayersForAddressRange(ipAddress);
    }
    return getAffectedPlayersForSingleAddress(ipAddress);
  }

  private Collection<Player> getAffectedPlayersForSingleAddress(String ipAddress) {
    return config.getBukkitWrapper().getOnlinePlayers().stream()
        .filter(player -> {
          if (player.getAddress() == null) {
            return false;
          }
          return ipAddress.equals(player.getAddress().getAddress().getHostAddress());
        }).collect(Collectors.toSet());
  }

  private Collection<Player> getAffectedPlayersForAddressRange(String ipAddressRange) {
    String[] twoIps = ipAddressRange.split("-");
    IpV4Wrapper minAddress = new IpV4Wrapper(twoIps[0]);
    IpV4Wrapper maxAddress = new IpV4Wrapper(twoIps[1]);
    IpRangeWrapper rangeIp = new IpRangeWrapper(minAddress, maxAddress);

    return config.getBukkitWrapper().getOnlinePlayers().stream()
        .filter(player -> {
          if (player.getAddress() == null) {
            return false;
          }
          return rangeIp.covers(new IpV4Wrapper(player.getAddress().getAddress().getHostAddress()));
        }).collect(Collectors.toSet());
  }
}
