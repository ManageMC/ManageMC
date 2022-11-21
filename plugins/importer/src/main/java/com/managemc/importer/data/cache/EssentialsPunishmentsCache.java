package com.managemc.importer.data.cache;

import com.managemc.importer.reader.model.EssentialsMute;
import com.managemc.importer.translation.EssentialsTypeTranslator;

public class EssentialsPunishmentsCache extends VanillaPunishmentsCache {

  public void add(EssentialsMute mute) {
    baseCache.addMute(EssentialsTypeTranslator.toMute(mute));
  }
}
