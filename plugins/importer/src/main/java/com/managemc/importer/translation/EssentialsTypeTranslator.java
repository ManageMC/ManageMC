package com.managemc.importer.translation;

import com.managemc.importer.reader.model.EssentialsMute;
import org.openapitools.client.model.ImportableBanOrMute;
import org.openapitools.client.model.ImportablePunishmentPlayer;

public class EssentialsTypeTranslator {

  public static final String ESSENTIALS_SOURCE = "Essentials plugin";

  public static ImportableBanOrMute toMute(EssentialsMute mute) {
    ImportablePunishmentPlayer offender = new ImportablePunishmentPlayer()
        .uuid(mute.getUuid())
        .username(mute.getUsername());

    long now = System.currentTimeMillis();


    return new ImportableBanOrMute()
        .offender(offender)
        .source(ESSENTIALS_SOURCE)
        .reason(mute.getReason())
        .issuedAtMillis(now)
        .durationMillis(mute.getMute() == 0 ? null : mute.getMute() - now);
  }
}
