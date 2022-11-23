package com.managemc.spigot.command.handler;

import com.managemc.api.ApiException;
import com.managemc.spigot.command.handler.base.CommandHandlerAsync;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.command.util.punishments.PunishmentFlags;
import com.managemc.spigot.command.util.punishments.PunishmentMessageSender;
import com.managemc.spigot.config.SpigotPluginConfig;
import org.bukkit.command.CommandSender;
import org.openapitools.client.model.BanOrMute;

import java.util.UUID;

public class CmdMuteHandler extends CommandHandlerAsync {

  private final SpigotPluginConfig config;

  public CmdMuteHandler(CommandSender sender, ProcessedCommandArguments args, SpigotPluginConfig config) {
    super(sender, args);
    this.config = config;
  }

  private PunishmentFlags flags;
  private PunishmentMessageSender messageSender;

  @Override
  public void preProcessCommand() {
    CommandAssertions.assertArgsLengthAtLeast(args.getArgs(), 1);
    CommandAssertions.assertValidUsername(args.get(0));

    flags = new PunishmentFlags(args);

    CommandAssertions.checkPermissionsForPunishmentFlags(flags, sender);

    this.messageSender = PunishmentMessageSender
        .forMutes(sender, config.getBukkitWrapper(), config.getServerGroupsData(), flags, args.get(0));

    messageSender.handlePublicMessage();
    messageSender.handleFlagMisuseWarnings();
  }

  @Override
  public void processCommandAsync() throws ApiException {
    UUID uuid = CommandAssertions.usernameToUuidBlocking(config.getUuidResolver(), args.get(0));

    if (uuid != null) {
      BanOrMute mute = config.getMuteService().mutePlayer(sender, args.get(0), uuid, flags);
      if (mute != null) {
        if (mute.getScope().size() > 0) {
          config.getServerGroupsData().refreshBlockingIfNeeded();
        }
        messageSender.sendIssuerMessage(mute);
      }
    }
  }
}
