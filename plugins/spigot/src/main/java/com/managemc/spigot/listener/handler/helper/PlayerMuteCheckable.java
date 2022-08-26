package com.managemc.spigot.listener.handler.helper;

import com.managemc.spigot.state.MuteManager;
import com.managemc.spigot.state.model.MuteData;
import com.managemc.spigot.util.chat.formatter.MuteDataFormatter;
import org.bukkit.entity.Player;

public abstract class PlayerMuteCheckable {

  protected final MuteManager muteManager;

  public PlayerMuteCheckable(MuteManager muteManager) {
    this.muteManager = muteManager;
  }

  protected MuteData fetchMuteDataAndHandleMute(Player player) {
    MuteData muteData = muteManager.getMuteData(player.getUniqueId());
    if (muteData != null && !muteData.isShadow()) {
      player.sendMessage(new MuteDataFormatter(muteData).format());
    }
    return muteData;
  }
}
