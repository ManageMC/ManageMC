package com.managemc.spigot.command.handler;

import com.managemc.api.ApiException;
import com.managemc.plugins.command.AbortCommand;
import com.managemc.spigot.command.handler.base.CommandHandlerAsync;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.command.util.punishments.PunishmentFlags;
import com.managemc.spigot.command.util.punishments.PunishmentMessageSender;
import com.managemc.spigot.command.util.validation.IpRangeWrapper;
import com.managemc.spigot.command.util.validation.IpV4Wrapper;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.util.KickMessages;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.openapitools.client.model.IpBan;

import java.util.Collection;
import java.util.stream.Collectors;

public class CmdBanIpHandler extends CommandHandlerAsync {

  public static final String OFFLINE_MSG = "This player has not been online recently enough for Bukkit to remember its IP address";
  public static final String NO_IP_MSG = "Bukkit does not know this player's IP address";

  private final SpigotPluginConfig config;

  public CmdBanIpHandler(CommandSender sender, ProcessedCommandArguments args, SpigotPluginConfig config) {
    super(sender, args);
    this.config = config;
  }

  private PunishmentFlags flags;
  private String ipAddress;
  private PunishmentMessageSender messageSender;

  @Override
  public void preProcessCommand() {
    CommandAssertions.assertArgsLengthAtLeast(args.getArgs(), 1);
    flags = new PunishmentFlags(args);
    CommandAssertions.checkPermissionsForPunishmentFlags(flags, sender);

    ipAddress = determineIpAddress();
    CommandAssertions.assertValidIpOrIpRange(ipAddress);

    messageSender = PunishmentMessageSender
        .forIpBans(sender, config.getBukkitWrapper(), config.getServerGroupsData(), flags, args.get(0));

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
  public void processCommandAsync() throws ApiException {
    IpBan ipBan = config.getIpBanService().banIpAddress(sender, ipAddress, flags);

    if (ipBan != null) {
      if (ipBan.getScope().size() > 0) {
        config.getServerGroupsData().refreshBlockingIfNeeded();
      }
      messageSender.sendIssuerMessage(ipBan);
    }
  }

  private String determineIpAddress() {
    if (args.get(0).contains(".")) {
      return args.get(0);
    }

    CommandAssertions.assertValidUsername(args.get(0));
    Player offender = config.getBukkitWrapper().getOnlineOrRecentlyOnlinePlayer(args.get(0));
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
