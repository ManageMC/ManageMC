package com.managemc.spigot.command.processor;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.processor.tabcompletion.PlayerPunishmentTabCompleter;
import com.managemc.spigot.command.util.CommandArgumentPreprocessor;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.command.util.punishments.PunishmentFlags;
import com.managemc.spigot.command.util.punishments.PunishmentMessageSender;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.util.KickMessages;
import com.managemc.spigot.util.permissions.PermissibleAction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.openapitools.client.model.BanOrMute;

import java.util.List;
import java.util.UUID;

public class CmdBan extends CommandExecutorAsync {

  private final SpigotPluginConfig config;
  private final PlayerPunishmentTabCompleter tabCompleter;

  public CmdBan(SpigotPluginConfig config) {
    super(config.getLogging());
    this.config = config;
    this.tabCompleter = new PlayerPunishmentTabCompleter(config.getBukkitWrapper());
  }

  private String username;
  private PunishmentFlags flags;
  private PunishmentMessageSender messageSender;

  @Override
  protected void preProcessCommand(CommandSender sender, String[] args) throws Exception {
    CommandAssertions.checkPermissions(PermissibleAction.BAN_PLAYER, sender);
    CommandAssertions.assertArgsLengthAtLeast(args, 1);
    username = CommandAssertions.assertValidUsername(args[0]);
    ProcessedCommandArguments processedArgs = CommandArgumentPreprocessor
        .preProcessArguments(PunishmentFlags.FLAGS, args);

    flags = new PunishmentFlags(processedArgs);
    CommandAssertions.checkPermissionsForPunishmentFlags(flags, sender);

    messageSender = PunishmentMessageSender
        .forBans(sender, config.getBukkitWrapper(), config.getServerGroupsData(), flags, username);

    Player onlineOffender = config.getBukkitWrapper().getOnlinePlayer(username);

    if (onlineOffender != null) {
      onlineOffender.kickPlayer(KickMessages.banMessage(flags.toBanOrMuteInput(config.getLocalConfig())));
    }

    messageSender.handlePublicMessage();
    messageSender.handleFlagMisuseWarnings();
  }

  @Override
  protected void onCommandAsync(CommandSender sender) throws Exception {
    UUID uuid = CommandAssertions.usernameToUuidBlocking(config.getUuidResolver(), username);

    if (uuid != null) {
      BanOrMute ban = config.getBanService().banPlayer(sender, username, uuid, flags);
      if (ban != null) {
        if (ban.getScope().size() > 0) {
          config.getServerGroupsData().refreshBlockingIfNeeded();
        }
        messageSender.sendIssuerMessage(ban);
      }
    }
  }

  @Override
  protected List<String> onTabComplete(CommandSender sender, String[] args) {
    if (PermissibleAction.BAN_PLAYER.isAllowed(sender)) {
      return tabCompleter.onTabComplete(sender, args);
    }
    return null;
  }
}
