package com.managemc.spigot.service;

import com.managemc.api.ApiException;
import com.managemc.spigot.command.util.punishments.PunishmentFlags;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.util.TextConstants;
import org.bukkit.command.CommandSender;
import org.openapitools.client.model.IpBan;

public class IpBanService {

  private final SpigotPluginConfig config;

  public IpBanService(SpigotPluginConfig config) {
    this.config = config;
  }

  public IpBan banIpAddress(CommandSender sender, String ipAddress, PunishmentFlags flags) throws ApiException {
    try {
      IpBan ipBan = config.getClientProvider().forCommandSender(sender).getPunishmentsApi()
          .createIpBan(ipAddress, flags.toIpBanInput(config.getLocalConfig()));
      config.getPardonablePunishmentData().addPunishment(sender, ipBan);
      return ipBan;
    } catch (ApiException e) {
      if (e.getCode() == 403) {
        sender.sendMessage(TextConstants.INSUFFICIENT_PERMS);
      } else {
        throw e;
      }
    }

    return null;
  }
}
