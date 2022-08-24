package com.managemc.importer.translation;

import com.managemc.plugins.util.RegexConstants;
import org.maxgamer.maxbans.orm.Restriction;
import org.maxgamer.maxbans.orm.User;
import org.openapitools.client.model.*;

public class MaxBansTypeTranslator {

  public static final String SOURCE = "MaxBansPlus plugin";
  public static final String PARDON_DETAILS = "Pardoned through MaxBansPlus plugin, which doesn't allow " +
      "the user to specify a reason";

  public static ImportableBanOrMute toBanOrMute(Restriction punishment) {
    return new ImportableBanOrMute()
        .reason(punishment.getReason())
        .offender(buildOffender(punishment))
        .issuer(buildUser(punishment.getSource()))
        .visibility(PunishmentVisibility.PUBLIC)
        .issuedAtMillis(punishment.getCreated().toEpochMilli())
        .durationMillis(buildDuration(punishment))
        .pardon(buildPardon(punishment))
        .source(SOURCE);
  }

  public static ImportableIpBan toIpBan(Restriction punishment) {
    return new ImportableIpBan()
        .minIpv4Address(punishment.getTenant().getName())
        .maxIpv4Address(punishment.getTenant().getName())
        .reason(punishment.getReason())
        .issuer(buildUser(punishment.getSource()))
        .visibility(PunishmentVisibility.PUBLIC)
        .issuedAtMillis(punishment.getCreated().toEpochMilli())
        .durationMillis(buildDuration(punishment))
        .pardon(buildPardon(punishment))
        .source(SOURCE);
  }

  public static ImportableWarning toWarning(Restriction punishment) {
    return new ImportableWarning()
        .reason(punishment.getReason())
        .offender(buildOffender(punishment))
        .issuer(buildUser(punishment.getSource()))
        .issuedAtMillis(punishment.getCreated().toEpochMilli())
        .pardon(buildPardon(punishment))
        .source(SOURCE);
  }

  /**
   * MaxBans does not guarantee that the id (UUID) field on the User table is
   * correct. There is an edge case in which, in offline mode, the plugin will
   * assign a random UUID to the player. Thus, to be certain of the integrity
   * of the data we import, we ignore the UUID and perform player UUID
   * resolution even though it may not be necessary in every case.
   */

  private static ImportablePunishmentPlayer buildOffender(Restriction punishment) {
    return new ImportablePunishmentPlayer()
        .username(punishment.getTenant().getName());
  }

  private static ImportablePunishmentPlayer buildUser(User user) {
    if (user == null) {
      return null;
    }

    if (!user.getName().matches(RegexConstants.USERNAME_REGEX)) {
      return null;
    }

    return new ImportablePunishmentPlayer().username(user.getName());
  }

  private static Long buildDuration(Restriction punishment) {
    if (punishment.getExpiresAt() == null) {
      return null;
    }

    return punishment.getExpiresAt().toEpochMilli() - punishment.getCreated().toEpochMilli();
  }

  private static ImportablePunishmentPardon buildPardon(Restriction punishment) {
    if (punishment.getRevokedAt() == null) {
      return null;
    }

    return new ImportablePunishmentPardon()
        .pardonedAtMillis(punishment.getRevokedAt().toEpochMilli())
        .details(PARDON_DETAILS)
        .pardoner(buildUser(punishment.getRevoker()));
  }
}
