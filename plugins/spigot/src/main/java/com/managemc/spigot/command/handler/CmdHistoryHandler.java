package com.managemc.spigot.command.handler;

import com.managemc.api.ApiException;
import com.managemc.spigot.command.handler.base.CommandHandlerAsync;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.util.chat.PluginMessageFormatter;
import com.managemc.spigot.util.chat.formatter.PlayerPunishmentFormatter;
import org.bukkit.command.CommandSender;
import org.openapitools.client.model.PlayerPunishment;

import java.util.List;
import java.util.UUID;

public class CmdHistoryHandler extends CommandHandlerAsync {

  public static final String NO_PUNISHMENTS_MESSAGE = new PluginMessageFormatter()
      .header("No punishments found for %s").toString();

  private final SpigotPluginConfig config;

  public CmdHistoryHandler(CommandSender sender, ProcessedCommandArguments args, SpigotPluginConfig config) {
    super(sender, args);
    this.config = config;
  }

  @Override
  public void preProcessCommand() {
    CommandAssertions.assertArgsLength(args.getArgs(), 1);
    CommandAssertions.assertValidUsername(args.get(0));
  }

  @Override
  public void processCommandAsync() throws ApiException {
    UUID uuid = CommandAssertions.usernameToUuidBlocking(config.getUuidResolver(), args.get(0));

    if (uuid != null) {
      List<PlayerPunishment> punishments = config.getPunishmentHistoryService().fetchPunishments(sender, args.get(0), uuid);

      if (punishments != null) {
        printPunishments(punishments, sender, args.get(0));
      }
    }
  }

  private void printPunishments(
      List<PlayerPunishment> punishments,
      CommandSender sender,
      String username
  ) throws ApiException {

    config.getServerGroupsData().refreshBlockingIfNeeded();
    if (punishments.size() == 0) {
      sender.sendMessage(String.format(NO_PUNISHMENTS_MESSAGE, username));
      return;
    }

    PluginMessageFormatter message = new PluginMessageFormatter().header("Punishment History for " + username);
    punishments.forEach(p -> {
      String punishment = new PlayerPunishmentFormatter(p, config.getServerGroupsData()).format();
      message.append(punishment).append("\n");
    });
    sender.sendMessage(message.toString());
  }
}
