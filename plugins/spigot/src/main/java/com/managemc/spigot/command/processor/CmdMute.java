package com.managemc.spigot.command.processor;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.processor.tabcompletion.PlayerPunishmentTabCompleter;
import com.managemc.spigot.command.util.CommandArgumentPreprocessor;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.command.util.punishments.PunishmentFlags;
import com.managemc.spigot.command.util.punishments.PunishmentMessageSender;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.util.permissions.PermissibleAction;
import org.bukkit.command.CommandSender;
import org.openapitools.client.model.BanOrMute;

import java.util.List;
import java.util.UUID;

public class CmdMute extends CommandExecutorAsync {

  private final SpigotPluginConfig config;
  private final PlayerPunishmentTabCompleter tabCompleter;

  public CmdMute(SpigotPluginConfig config) {
    super(config.getLogging());
    this.config = config;
    this.tabCompleter = new PlayerPunishmentTabCompleter(config.getBukkitWrapper());
  }

  private String username;
  private PunishmentFlags flags;
  private PunishmentMessageSender messageSender;

  @Override
  protected void preProcessCommand(CommandSender sender, String[] args) throws Exception {
    CommandAssertions.checkPermissions(PermissibleAction.MUTE_PLAYER, sender);
    CommandAssertions.assertArgsLengthAtLeast(args, 1);
    username = CommandAssertions.assertValidUsername(args[0]);
    ProcessedCommandArguments processedArgs = CommandArgumentPreprocessor
        .preProcessArguments(PunishmentFlags.FLAGS, args);

    flags = new PunishmentFlags(processedArgs);
    CommandAssertions.checkPermissionsForPunishmentFlags(flags, sender);

    messageSender = PunishmentMessageSender
        .forMutes(sender, config.getBukkitWrapper(), config.getServerGroupsData(), flags, username);

    messageSender.handlePublicMessage();
    messageSender.handleFlagMisuseWarnings();
  }

  @Override
  protected void onCommandAsync(CommandSender sender) throws Exception {
    UUID uuid = CommandAssertions.usernameToUuidBlocking(config.getUuidResolver(), username);

    if (uuid != null) {
      BanOrMute mute = config.getMuteService().mutePlayer(sender, username, uuid, flags);
      if (mute != null) {
        if (mute.getScope().size() > 0) {
          config.getServerGroupsData().refreshBlockingIfNeeded();
        }
        messageSender.sendIssuerMessage(mute);
      }
    }
  }

  @Override
  protected List<String> onTabComplete(CommandSender sender, String[] args) {
    return tabCompleter.onTabComplete(sender, args);
  }
}
