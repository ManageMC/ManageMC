package com.managemc.importer.data.cache;

import com.managemc.importer.translation.AdvancedBanTypeTranslator;
import lombok.Getter;
import me.leoko.advancedban.utils.Punishment;

public class AdvancedBanPunishmentsCache {

  @Getter
  private final PunishmentsCache baseCache;

  public AdvancedBanPunishmentsCache() {
    this.baseCache = new PunishmentsCache();
  }

  public void add(Punishment punishment, boolean pardoned) {
    // NOTE and KICK are not supported (yet)
    switch (punishment.getType()) {
      case BAN:
      case TEMP_BAN:
        baseCache.addBan(AdvancedBanTypeTranslator.toBanOrMute(punishment, pardoned));
        break;
      case IP_BAN:
      case TEMP_IP_BAN:
        baseCache.addIpBan(AdvancedBanTypeTranslator.toIpBan(punishment, pardoned));
        break;
      case MUTE:
      case TEMP_MUTE:
        baseCache.addMute(AdvancedBanTypeTranslator.toBanOrMute(punishment, pardoned));
        break;
      case WARNING:
      case TEMP_WARNING:
        baseCache.addWarning(AdvancedBanTypeTranslator.toWarning(punishment, pardoned));
        break;
    }
  }
}
