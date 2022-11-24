package com.managemc.spigot.command.processor;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.util.CommandArgumentPreprocessor;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.util.KickMessages;
import com.managemc.spigot.util.chat.formatter.WarningSuccessFormatter;
import com.managemc.spigot.util.permissions.PermissibleAction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.openapitools.client.model.Warning;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CmdWarn extends CommandExecutorAsync {

  private final SpigotPluginConfig config;

  public CmdWarn(SpigotPluginConfig config) {
    super(config.getLogging());
    this.config = config;
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
    details = determineDetails(processedArgs);

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
    switch (args.length) {
      case 0:
        return getMatchingOnlinePlayers("");
      case 1:
        return getMatchingOnlinePlayers(args[0]);
      default:
        return null;
    }
  }

  private String determineDetails(String[] args) {
    if (args.length < 2) {
      return null;
    }

    String details = Arrays
        .stream(args, 2, args.length)
        .collect(Collectors.joining(" "));

    if (details.length() == 0) {
      return null;
    }

    return details;
  }

  private List<String> getMatchingOnlinePlayers(String lastArg) {
    return config.getBukkitWrapper().getOnlinePlayers().stream()
        .map(Player::getName)
        .filter(name -> name.toLowerCase().startsWith(lastArg.toLowerCase()))
        .sorted()
        .collect(Collectors.toList());
  }
}
