package com.managemc.spigot.command.processor;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.processor.tabcompletion.OnlinePlayersTabCompleter;
import com.managemc.spigot.command.util.CommandArgumentPreprocessor;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.MultiWordArguments;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.util.KickMessages;
import com.managemc.spigot.util.chat.formatter.WarningSuccessFormatter;
import com.managemc.spigot.util.permissions.PermissibleAction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.openapitools.client.model.Warning;

import java.util.List;
import java.util.UUID;

public class CmdWarn extends CommandExecutorAsync {

  private final SpigotPluginConfig config;
  private final OnlinePlayersTabCompleter tabCompleter;

  public CmdWarn(SpigotPluginConfig config) {
    super(config.getLogging());
    this.config = config;
    this.tabCompleter = new OnlinePlayersTabCompleter(config.getBukkitWrapper());
  }

  private String username;
  private String reason;
  private String details;

  @Override
  protected void preProcessCommand(CommandSender sender, String[] args) throws Exception {
    CommandAssertions.checkPermissions(PermissibleAction.WARN_PLAYER, sender);
    CommandAssertions.assertArgsLengthAtLeast(args, 1);
    username = CommandAssertions.assertValidUsername(args[0]);
    String[] processedArgs = CommandArgumentPreprocessor.processDoubleQuotes(args);

    reason = processedArgs.length > 1 ? processedArgs[1] : null;
    details = MultiWordArguments.startingAtIndex(args, 1);

    Player onlineOffender = config.getBukkitWrapper().getOnlinePlayer(args[0]);

    if (onlineOffender != null) {
      onlineOffender.kickPlayer(KickMessages.warningMessage(reason));
    }
  }

  @Override
  protected void onCommandAsync(CommandSender sender) throws Exception {
    UUID uuid = CommandAssertions.usernameToUuidBlocking(config.getUuidResolver(), username);

    if (uuid != null) {
      Warning warning = config.getWarningService().warnPlayer(sender, username, uuid, reason, details);
      if (warning != null) {
        String message = new WarningSuccessFormatter(warning).format();
        sender.sendMessage(message);
      }
    }
  }

  @Override
  protected List<String> onTabComplete(CommandSender sender, String[] args) {
    if (PermissibleAction.WARN_PLAYER.isAllowed(sender)) {
      return tabCompleter.onTabComplete(args);
    }
    return null;
  }
}
