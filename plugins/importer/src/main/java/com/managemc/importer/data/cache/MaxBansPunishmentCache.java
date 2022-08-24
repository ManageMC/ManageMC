package com.managemc.importer.data.cache;

import com.managemc.importer.reader.model.MaxBansPlusPunishmentType;
import com.managemc.importer.translation.MaxBansTypeTranslator;
import lombok.Getter;
import org.maxgamer.maxbans.orm.Restriction;

public class MaxBansPunishmentCache {

  @Getter
  private final PunishmentsCache baseCache;

  public MaxBansPunishmentCache() {
    this.baseCache = new PunishmentsCache();
  }

  public void add(Restriction punishment, MaxBansPlusPunishmentType type) {
    switch (type) {
      case BAN:
        baseCache.addBan(MaxBansTypeTranslator.toBanOrMute(punishment));
        break;
      case MUTE:
        baseCache.addMute(MaxBansTypeTranslator.toBanOrMute(punishment));
        break;
      case WARNING:
        baseCache.addWarning(MaxBansTypeTranslator.toWarning(punishment));
        break;
      case IP_BAN:
        baseCache.addIpBan(MaxBansTypeTranslator.toIpBan(punishment));
        break;
    }
  }
}
