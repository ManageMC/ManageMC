package com.managemc.spigot.command.handler;

import com.managemc.api.ApiException;
import com.managemc.spigot.command.handler.base.CommandHandlerAsync;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.command.util.punishments.PunishmentFlags;
import com.managemc.spigot.command.util.punishments.PunishmentMessageSender;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.util.KickMessages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.openapitools.client.model.BanOrMute;

import java.util.UUID;

public class CmdBanHandler extends CommandHandlerAsync {

  private final SpigotPluginConfig config;

  public CmdBanHandler(CommandSender sender, ProcessedCommandArguments args, SpigotPluginConfig config) {
    super(sender, args);
    this.config = config;
  }

  private PunishmentFlags flags;
  private PunishmentMessageSender messageSender;

  @Override
  public void preProcessCommand() {
    CommandAssertions.assertArgsLengthAtLeast(args.getArgs(), 1);
    CommandAssertions.assertValidUsername(args.get(0));

    this.flags = new PunishmentFlags(args);

    CommandAssertions.checkPermissionsForPunishmentFlags(flags, sender);

    this.messageSender = PunishmentMessageSender
        .forBans(sender, config.getBukkitWrapper(), config.getServerGroupsData(), flags, args.get(0));

    Player onlineOffender = config.getBukkitWrapper().getOnlinePlayer(args.get(0));

    if (onlineOffender != null) {
      onlineOffender.kickPlayer(KickMessages.banMessage(flags.toBanOrMuteInput(config.getLocalConfig())));
    }

    messageSender.handlePublicMessage();
    messageSender.handleFlagMisuseWarnings();
  }

  @Override
  public void processCommandAsync() throws ApiException {
    UUID uuid = CommandAssertions.usernameToUuidBlocking(config.getUuidResolver(), args.get(0));

    if (uuid != null) {
      BanOrMute ban = config.getBanService().banPlayer(sender, args.get(0), uuid, flags);
      if (ban != null) {
        if (ban.getScope().size() > 0) {
          config.getServerGroupsData().refreshBlockingIfNeeded();
        }
        messageSender.sendIssuerMessage(ban);
      }
    }
  }
}
