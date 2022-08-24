package com.managemc.spigot.service;

import com.managemc.api.ApiException;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.util.TextConstants;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.openapitools.client.model.PardonPunishmentsInput;
import org.openapitools.client.model.PardonPunishmentsResponse;
import org.openapitools.client.model.PunishmentIdAndType;

import java.util.Collection;
import java.util.Objects;

public class PardonService {

  public static final String SUCCESS_MESSAGE = ChatColor.GREEN + "Successfully issued %s pardon(s)";
  public static final String INACTIVE_PUNISHMENTS_WARNING = ChatColor.YELLOW +
      "Note: some punishment(s) were already inactive and thus did not get pardoned.";
  public static final String NOT_FOUND_MESSAGE = ChatColor.RED + "One or more punishments not found";
  public static final String NO_LONGER_MUTED_MESSAGE = ChatColor.GREEN + "You are no longer muted";

  private final SpigotPluginConfig config;

  public PardonService(SpigotPluginConfig config) {
    this.config = config;
  }

  public void issuePardon(Collection<String> punishmentIds, String details, CommandSender sender) throws ApiException {
    PardonPunishmentsInput input = new PardonPunishmentsInput()
        .pardonDetails(details);

    punishmentIds.forEach(id -> {
      String[] splitId = id.split("-");
      PunishmentIdAndType.TypeEnum type = PunishmentIdAndType.TypeEnum.fromValue(splitId[0].toUpperCase());
      long punishmentId = Long.parseLong(splitId[1]);
      PunishmentIdAndType punishment = new PunishmentIdAndType()
          .id(punishmentId)
          .type(type);
      input.addPunishmentsItem(punishment);
    });

    try {
      PardonPunishmentsResponse response = config.getClientProvider().forCommandSender(sender).getPunishmentsApi().pardonPunishments(input);
      config.getPardonablePunishmentData().clearPardonedPunishments(punishmentIds);
      handleSuccessMessage(sender, punishmentIds.size(), response);
      config.getMuteManager().removeMutes(punishmentIds).stream()
          .map(config.getBukkitWrapper()::getOnlinePlayer)
          .filter(Objects::nonNull)
          .forEach(player -> player.sendMessage(NO_LONGER_MUTED_MESSAGE));
    } catch (ApiException e) {
      switch (e.getCode()) {
        case 403:
          sender.sendMessage(TextConstants.INSUFFICIENT_PERMS);
          break;
        case 422:
          sender.sendMessage(NOT_FOUND_MESSAGE);
          break;
        default:
          throw e;
      }
    }
  }

  private void handleSuccessMessage(CommandSender sender, int size, PardonPunishmentsResponse response) {
    sender.sendMessage(String.format(SUCCESS_MESSAGE, size));
    if (response.getInactivePunishmentsFound()) {
      sender.sendMessage(INACTIVE_PUNISHMENTS_WARNING);
    }
  }
}
