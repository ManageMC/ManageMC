package com.managemc.spigot.command.util.punishments;

import com.managemc.plugins.bukkit.BukkitWrapper;
import com.managemc.spigot.state.ServerGroupsData;
import com.managemc.spigot.util.TextConstants;
import com.managemc.spigot.util.chat.formatter.BanSuccessFormatter;
import com.managemc.spigot.util.chat.formatter.IpBanSuccessFormatter;
import com.managemc.spigot.util.chat.formatter.MuteSuccessFormatter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.openapitools.client.model.BanOrMute;
import org.openapitools.client.model.IpBan;

import java.util.HashSet;
import java.util.Set;

public class PunishmentMessageSender {

  private final CommandSender sender;
  private final BukkitWrapper bukkitWrapper;
  private final ServerGroupsData serverGroups;
  private final PunishmentFlags flags;
  private final String username;
  private final String verb;

  private PunishmentMessageSender(
      CommandSender sender,
      BukkitWrapper bukkitWrapper,
      ServerGroupsData serverGroups,
      PunishmentFlags flags,
      String username,
      String verb
  ) {
    this.sender = sender;
    this.bukkitWrapper = bukkitWrapper;
    this.serverGroups = serverGroups;
    this.flags = flags;
    this.username = username;
    this.verb = verb;
  }

  public static PunishmentMessageSender forBans(
      CommandSender sender,
      BukkitWrapper bukkitWrapper,
      ServerGroupsData serverGroups,
      PunishmentFlags flags,
      String username
  ) {
    return new PunishmentMessageSender(sender, bukkitWrapper, serverGroups, flags, username, "banned");
  }

  public static PunishmentMessageSender forMutes(
      CommandSender sender,
      BukkitWrapper bukkitWrapper,
      ServerGroupsData serverGroups,
      PunishmentFlags flags,
      String username
  ) {
    return new PunishmentMessageSender(sender, bukkitWrapper, serverGroups, flags, username, "muted");
  }

  public static PunishmentMessageSender forIpBans(
      CommandSender sender,
      BukkitWrapper bukkitWrapper,
      ServerGroupsData serverGroups,
      PunishmentFlags flags,
      String username
  ) {
    return new PunishmentMessageSender(sender, bukkitWrapper, serverGroups, flags, username, "banned");
  }

  public void sendIssuerMessage(BanOrMute punishment) {
    switch (punishment.getType()) {
      case BAN:
        sender.sendMessage(new BanSuccessFormatter(punishment, serverGroups).format());
        break;
      case MUTE:
        sender.sendMessage(new MuteSuccessFormatter(punishment, serverGroups).format());
        break;
      default:
        throw new RuntimeException("Unexpected BanOrMute.TypeEnum " + punishment.getType());
    }
  }

  public void sendIssuerMessage(IpBan punishment) {
    String message = new IpBanSuccessFormatter(punishment, serverGroups).format();
    sender.sendMessage(message);
  }

  public void handlePublicMessage(String username) {
    if (flags.isBroadcast() && !flags.isPrivate() && !flags.isShadow()) {
      String message = ChatColor.GRAY + username + " has been " + verb;
      bukkitWrapper.broadcastMessage(message);
    }
  }

  public void handlePublicMessage() {
    handlePublicMessage(username);
  }

  public void handleFlagMisuseWarnings() {
    getFlagMisuseWarnings().forEach(sender::sendMessage);
  }

  private Set<String> getFlagMisuseWarnings() {
    Set<String> warnings = new HashSet<>();
    if (flags.isBroadcast() && (flags.isShadow() || flags.isPrivate())) {
      warnings.add(TextConstants.Commands.BROADCAST_FLAG_IGNORED_MESSAGE);
    }
    if (flags.isShadow() && flags.isPrivate()) {
      warnings.add(TextConstants.Commands.PRIVATE_FLAG_REDUNDANT_MESSAGE);
    }
    return warnings;
  }
}
