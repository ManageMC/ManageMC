package com.managemc.spigot.service;

import com.managemc.api.ApiException;
import com.managemc.spigot.command.util.punishments.PunishmentFlags;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.util.TextConstants;
import org.bukkit.command.CommandSender;
import org.openapitools.client.model.BanOrMute;

import java.util.UUID;

public class BanService {

  private final SpigotPluginConfig config;

  public BanService(SpigotPluginConfig config) {
    this.config = config;
  }

  public BanOrMute banPlayer(CommandSender sender, String username, UUID uuid, PunishmentFlags flags) throws ApiException {
    try {
      BanOrMute ban = config.getClientProvider().forCommandSender(sender).getPunishmentsApi()
          .createBan(uuid.toString(), flags.toBanOrMuteInput(config.getLocalConfig()));
      config.getPardonablePunishmentData().addPunishment(sender, ban);
      return ban;
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
