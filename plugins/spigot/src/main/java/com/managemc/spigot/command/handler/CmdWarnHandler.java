package com.managemc.spigot.command.handler;

import com.managemc.api.ApiException;
import com.managemc.spigot.command.handler.base.CommandHandlerAsync;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.util.KickMessages;
import com.managemc.spigot.util.chat.formatter.WarningSuccessFormatter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.openapitools.client.model.Warning;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

public class CmdWarnHandler extends CommandHandlerAsync {

  private final SpigotPluginConfig config;

  public CmdWarnHandler(CommandSender sender, ProcessedCommandArguments args, SpigotPluginConfig config) {
    super(sender, args);
    this.config = config;
  }

  private String reason;

  @Override
  public void preProcessCommand() {
    CommandAssertions.assertArgsLengthAtLeast(args.getArgs(), 1);
    CommandAssertions.assertValidUsername(args.get(0));

    reason = args.get(1);

    Player onlineOffender = config.getBukkitWrapper().getOnlinePlayer(args.get(0));

    if (onlineOffender != null) {
      onlineOffender.kickPlayer(KickMessages.warningMessage(reason));
    }
  }

  @Override
  public void processCommandAsync() throws ApiException {
    UUID uuid = CommandAssertions.usernameToUuidBlocking(config.getUuidResolver(), args.get(0));

    if (uuid != null) {
      Warning warning = config.getWarningService().warnPlayer(sender, args.get(0), uuid, reason, determineDetails());
      if (warning != null) {
        String message = new WarningSuccessFormatter(warning).format();
        sender.sendMessage(message);
      }
    }
  }

  private String determineDetails() {
    if (args.length() < 2) {
      return null;
    }

    String details = Arrays
        .stream(args.getArgs(), 2, args.getArgs().length)
        .collect(Collectors.joining(" "));

    if (details.length() == 0) {
      return null;
    }

    return details;
  }
}
