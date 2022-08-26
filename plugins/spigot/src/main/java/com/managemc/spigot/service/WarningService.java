package com.managemc.spigot.service;

import com.managemc.api.ApiException;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.util.TextConstants;
import org.bukkit.command.CommandSender;
import org.openapitools.client.model.CreateWarningInput;
import org.openapitools.client.model.Warning;

import java.util.UUID;

public class WarningService {

  private final SpigotPluginConfig config;

  public WarningService(SpigotPluginConfig config) {
    this.config = config;
  }

  public Warning warnPlayer(CommandSender sender, String username, UUID uuid, String reason, String details) throws ApiException {
    CreateWarningInput input = new CreateWarningInput()
        .reason(reason)
        .details(details);

    try {
      Warning warning = config.getClientProvider().forCommandSender(sender).getPunishmentsApi()
          .createWarning(uuid.toString(), input);
      config.getPardonablePunishmentData().addPunishment(sender, warning);
      return warning;
    } catch (ApiException e) {
      switch (e.getCode()) {
        case 403:
          sender.sendMessage(TextConstants.INSUFFICIENT_PERMS);
          break;
        case 404:
          sender.sendMessage(String.format(TextConstants.Commands.Punishment.PUNISHMENT_404, username));
          break;
        default:
          throw e;
      }
    }

    return null;
  }
}
