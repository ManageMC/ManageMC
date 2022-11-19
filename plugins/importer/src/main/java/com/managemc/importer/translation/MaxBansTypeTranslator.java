package com.managemc.importer.translation;

import com.managemc.plugins.util.RegexConstants;
import org.maxgamer.maxbans.orm.Restriction;
import org.maxgamer.maxbans.orm.Tenant;
import org.maxgamer.maxbans.orm.User;
import org.openapitools.client.model.*;

public class MaxBansTypeTranslator {

  public static final String SOURCE = "MaxBansPlus plugin";
  public static final String PARDON_DETAILS = "Pardoned through MaxBansPlus plugin, which doesn't allow " +
      "the user to specify a reason";

  public static ImportableBanOrMute toBanOrMute(Restriction punishment) {
    return new ImportableBanOrMute()
        .reason(punishment.getReason())
        .offender(buildTenant(punishment.getTenant()))
        .issuer(buildTenant(punishment.getSource()))
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
        .issuer(buildTenant(punishment.getSource()))
        .visibility(PunishmentVisibility.PUBLIC)
        .issuedAtMillis(punishment.getCreated().toEpochMilli())
        .durationMillis(buildDuration(punishment))
        .pardon(buildPardon(punishment))
        .source(SOURCE);
  }

  public static ImportableWarning toWarning(Restriction punishment) {
    return new ImportableWarning()
        .reason(punishment.getReason())
        .offender(buildTenant(punishment.getTenant()))
        .issuer(buildTenant(punishment.getSource()))
        .issuedAtMillis(punishment.getCreated().toEpochMilli())
        .pardon(buildPardon(punishment))
        .source(SOURCE);
  }

  private static ImportablePunishmentPlayer buildTenant(Tenant tenant) {
    if (tenant == null) {
      return null;
    }

    if (!tenant.getName().matches(RegexConstants.USERNAME_REGEX)) {
      return null;
    }

    ImportablePunishmentPlayer player = new ImportablePunishmentPlayer()
        .username(tenant.getName());

    if (tenant instanceof User) {
      return player.uuid(((User) tenant).getId());
    }

    return player;
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
        .pardoner(buildTenant(punishment.getRevoker()));
  }
}
