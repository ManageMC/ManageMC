package com.managemc.importer.translation;

import com.managemc.importer.reader.constant.PunishmentReaderConstants;
import com.managemc.importer.reader.model.VanillaBan;
import com.managemc.importer.reader.model.VanillaIpBan;
import com.managemc.importer.translation.util.TimestampToMillisConverter;
import com.managemc.plugins.util.RegexConstants;
import com.managemc.plugins.util.UuidHyphenator;
import lombok.SneakyThrows;
import org.openapitools.client.model.ImportableBanOrMute;
import org.openapitools.client.model.ImportableIpBan;
import org.openapitools.client.model.ImportablePunishmentPlayer;

public class VanillaTypeTranslator {

  public static final String VANILLA_BAN_LIST_SOURCE = "Vanilla ban list";
  public static final String VANILLA_IP_BAN_LIST_SOURCE = "Vanilla IP ban list";

  public static ImportableBanOrMute toBan(VanillaBan ban) {
    return new ImportableBanOrMute()
        .offender(buildOffender(ban))
        .issuer(buildIssuer(ban.getSource()))
        .reason(ban.getReason())
        .durationMillis(buildDuration(ban.getCreated(), ban.getExpires()))
        .issuedAtMillis(toMillis(ban.getCreated()))
        .source(VANILLA_BAN_LIST_SOURCE);
  }

  public static ImportableIpBan toIpBan(VanillaIpBan ipBan) {
    return new ImportableIpBan()
        .minIpv4Address(ipBan.getIp())
        .maxIpv4Address(ipBan.getIp())
        .issuer(buildIssuer(ipBan.getSource()))
        .reason(ipBan.getReason())
        .durationMillis(buildDuration(ipBan.getCreated(), ipBan.getExpires()))
        .issuedAtMillis(toMillis(ipBan.getCreated()))
        .source(VANILLA_IP_BAN_LIST_SOURCE);
  }

  @SneakyThrows
  private static long toMillis(String timestamp) {
    return TimestampToMillisConverter.toMillis(timestamp);
  }

  private static ImportablePunishmentPlayer buildIssuer(String source) {
    if (source == null) {
      return null;
    }

    if (PunishmentReaderConstants.VANILLA_CONSOLE_USERS.contains(source)) {
      return null;
    }

    if (!source.matches(RegexConstants.USERNAME_REGEX)) {
      return null;
    }

    return new ImportablePunishmentPlayer()
        .username(source)
        .uuid(null);
  }

  private static ImportablePunishmentPlayer buildOffender(VanillaBan ban) {
    return new ImportablePunishmentPlayer()
        .username(ban.getName())
        .uuid(UuidHyphenator.toUUID(ban.getUuid()));
  }

  private static Long buildDuration(String created, String expires) {
    if (expires == null) {
      return null;
    }

    if (expires.equals(PunishmentReaderConstants.VANILLA_PERMBAN_EXPIRES_FIELD)) {
      return null;
    }

    return toMillis(expires) - toMillis(created);
  }
}
