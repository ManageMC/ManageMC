package com.managemc.importer.data.cache;

import com.managemc.importer.reader.model.VanillaBan;
import com.managemc.importer.reader.model.VanillaIpBan;
import com.managemc.importer.translation.VanillaTypeTranslator;
import lombok.Getter;

public class VanillaPunishmentsCache {

  @Getter
  private final PunishmentsCache baseCache;

  public VanillaPunishmentsCache() {
    this.baseCache = new PunishmentsCache();
  }

  public void add(VanillaBan ban) {
    baseCache.addBan(VanillaTypeTranslator.toBan(ban));
  }

  public void add(VanillaIpBan ipBan) {
    baseCache.addIpBan(VanillaTypeTranslator.toIpBan(ipBan));
  }
}
