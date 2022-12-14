package com.managemc.spigot.command;

import com.managemc.api.ApiException;
import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.tabcompletion.OnlinePlayersTabCompleter;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.util.chat.PluginMessageFormatter;
import com.managemc.spigot.util.chat.formatter.PlayerPunishmentFormatter;
import com.managemc.spigot.util.permissions.PermissibleAction;
import org.bukkit.command.CommandSender;
import org.openapitools.client.model.PlayerPunishment;

import java.util.List;
import java.util.UUID;

public class CmdHistory extends CommandExecutorAsync {

  public static final String NO_PUNISHMENTS_MESSAGE = new PluginMessageFormatter()
      .header("No punishments found for %s").toString();

  private final SpigotPluginConfig config;
  private final OnlinePlayersTabCompleter tabCompleter;

  public CmdHistory(SpigotPluginConfig config) {
    super(config.getLogging());
    this.config = config;
    this.tabCompleter = new OnlinePlayersTabCompleter(config.getBukkitWrapper());
  }

  private String username;

  @Override
  protected void preProcessCommand(CommandSender sender, String[] args) throws Exception {
    CommandAssertions.checkPermissions(PermissibleAction.FETCH_PUNISHMENT_HISTORY, sender);
    CommandAssertions.assertArgsLength(args, 1);
    username = CommandAssertions.assertValidUsername(args[0]);
  }

  @Override
  protected void onCommandAsync(CommandSender sender) throws Exception {
    UUID uuid = CommandAssertions.usernameToUuidBlocking(config.getUuidResolver(), username);

    if (uuid != null) {
      List<PlayerPunishment> punishments = config.getPunishmentHistoryService()
          .fetchPunishments(sender, username, uuid);

      if (punishments != null) {
        printPunishments(punishments, sender, username);
      }
    }
  }

  @Override
  protected List<String> onTabComplete(CommandSender sender, String[] args) {
    if (PermissibleAction.FETCH_PUNISHMENT_HISTORY.isAllowed(sender)) {
      return tabCompleter.onTabComplete(args);
    }
    return null;
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
