package com.managemc.spigot.command.handler;

import com.managemc.api.ApiException;
import com.managemc.spigot.command.handler.base.CommandHandlerAsync;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.config.SpigotPluginConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class CmdCreatePlayerHandler extends CommandHandlerAsync {

  public static final String SUCCESS_MSG = ChatColor.GREEN + "Successfully added %s to the ManageMC database";

  private final SpigotPluginConfig config;

  public CmdCreatePlayerHandler(CommandSender sender, ProcessedCommandArguments args, SpigotPluginConfig config) {
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
      config.getCreatePlayerService().createPlayer(args.get(0), uuid);
    }

    sender.sendMessage(String.format(SUCCESS_MSG, args.get(0)));
  }
}
