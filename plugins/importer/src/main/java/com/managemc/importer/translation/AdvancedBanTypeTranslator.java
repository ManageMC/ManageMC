package com.managemc.importer.translation;

import com.managemc.importer.reader.constant.PunishmentReaderConstants;
import com.managemc.plugins.util.RegexConstants;
import com.managemc.plugins.util.UuidHyphenator;
import me.leoko.advancedban.utils.Punishment;
import org.openapitools.client.model.*;

public class AdvancedBanTypeTranslator {

  public static final String ADVANCEDBAN_SOURCE = "AdvancedBan plugin";
  public static final String PARDON_DETAILS = "Pardoned at an unknown time while server was still using AdvancedBan.";

  public static ImportableBanOrMute toBanOrMute(Punishment punishment, boolean pardoned) {
    return new ImportableBanOrMute()
        .offender(buildOffender(punishment))
        .issuer(buildIssuer(punishment))
        .visibility(PunishmentVisibility.PUBLIC)
        .scope(null)
        .reason(punishment.getReason())
        .details(null)
        .durationMillis(buildDuration(punishment))
        .issuedAtMillis(punishment.getStart())
        .pardon(pardoned ? buildPardon(punishment) : null)
        .source(ADVANCEDBAN_SOURCE);
  }

  public static ImportableWarning toWarning(Punishment punishment, boolean pardoned) {
    return new ImportableWarning()
        .offender(buildOffender(punishment))
        .issuer(buildIssuer(punishment))
        .reason(punishment.getReason())
        .details(null)
        .issuedAtMillis(punishment.getStart())
        .pardon(pardoned ? buildPardon(punishment) : null)
        .source(ADVANCEDBAN_SOURCE);
  }

  public static ImportableIpBan toIpBan(Punishment punishment, boolean pardoned) {
    return new ImportableIpBan()
        .minIpv4Address(punishment.getName())
        .maxIpv4Address(punishment.getName())
        .issuer(buildIssuer(punishment))
        .visibility(PunishmentVisibility.PUBLIC)
        .scope(null)
        .reason(punishment.getReason())
        .details(null)
        .durationMillis(buildDuration(punishment))
        .issuedAtMillis(punishment.getStart())
        .pardon(pardoned ? buildPardon(punishment) : null)
        .source(ADVANCEDBAN_SOURCE);
  }

  private static ImportablePunishmentPlayer buildOffender(Punishment punishment) {
    return new ImportablePunishmentPlayer()
        .username(punishment.getName())
        .uuid(UuidHyphenator.toUUID(punishment.getUuid()));
  }

  private static ImportablePunishmentPlayer buildIssuer(Punishment punishment) {
    if (punishment.getOperator() == null) {
      return null;
    }

    if (PunishmentReaderConstants.ADVANCEDBAN_CONSOLE_USER.equals(punishment.getOperator())) {
      return null;
    }

    if (!punishment.getOperator().matches(RegexConstants.USERNAME_REGEX)) {
      return null;
    }

    return new ImportablePunishmentPlayer()
        .username(punishment.getOperator());
  }

  private static Long buildDuration(Punishment punishment) {
    return punishment.getType().isTemp()
        ? punishment.getEnd() - punishment.getStart()
        : null;
  }

  private static ImportablePunishmentPardon buildPardon(Punishment punishment) {
    return new ImportablePunishmentPardon()
        .pardoner(null)
        .pardonedAtMillis(punishment.getStart())
        .details(PARDON_DETAILS);
  }
}
