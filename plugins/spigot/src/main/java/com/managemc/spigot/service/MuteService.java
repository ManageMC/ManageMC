package com.managemc.spigot.service;

import com.managemc.api.ApiException;
import com.managemc.spigot.command.util.punishments.PunishmentFlags;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.state.model.MuteData;
import com.managemc.spigot.util.TextConstants;
import com.managemc.spigot.util.chat.formatter.MuteDataFormatter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.openapitools.client.model.BanOrMute;

import java.util.UUID;

public class MuteService {

  private final SpigotPluginConfig config;

  public MuteService(SpigotPluginConfig config) {
    this.config = config;
  }

  public BanOrMute mutePlayer(CommandSender sender, String username, UUID uuid, PunishmentFlags flags) throws ApiException {
    Player onlineOffender = config.getBukkitWrapper().getOnlinePlayer(username);

    try {
      BanOrMute mute = config.getClientProvider().forCommandSender(sender).getPunishmentsApi()
          .createMute(uuid.toString(), flags.toBanOrMuteInput(config.getLocalConfig()));
      config.getPardonablePunishmentData().addPunishment(sender, mute);

      if (onlineOffender != null && onlineOffender.isOnline()) {
        MuteData muteData = new MuteData(uuid, mute);
        config.getMuteManager().setMuted(muteData);

        if (!flags.isShadow()) {
          onlineOffender.sendMessage(new MuteDataFormatter(muteData).format());
        }
      }

      return mute;
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
