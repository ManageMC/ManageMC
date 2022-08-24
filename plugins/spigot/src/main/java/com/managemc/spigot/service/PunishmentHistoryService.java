package com.managemc.spigot.service;

import com.managemc.api.ApiException;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.util.TextConstants;
import com.managemc.spigot.util.comparator.PlayerPunishmentComparator;
import org.bukkit.command.CommandSender;
import org.openapitools.client.model.PlayerPunishment;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PunishmentHistoryService {

  private final SpigotPluginConfig config;

  public PunishmentHistoryService(SpigotPluginConfig config) {
    this.config = config;
  }

  public List<PlayerPunishment> fetchPunishments(CommandSender sender, String username, UUID uuid) throws ApiException {
    try {
      List<PlayerPunishment> punishments = config.getClientProvider().forCommandSender(sender).getPunishmentsApi()
          .fetchPlayerPunishmentHistory(uuid.toString());
      config.getPardonablePunishmentData().addPunishments(sender, punishments);
      return punishments.stream()
          .sorted(new PlayerPunishmentComparator())
          .collect(Collectors.toList());

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
