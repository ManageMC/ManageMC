package com.managemc.spigot.state.model;

import lombok.Getter;
import org.openapitools.client.model.BanOrMute;
import org.openapitools.client.model.CursoryPlayerInfo;
import org.openapitools.client.model.PunishmentVisibility;

import java.util.Optional;
import java.util.UUID;

public class MuteData {

  @Getter
  private final UUID uuid;
  @Getter
  private final String reason;
  @Getter
  private final boolean shadow;
  @Getter
  private final Long expiresAt;
  @Getter
  private final UUID issuerId;
  @Getter
  private final String id;

  public MuteData(UUID onlinePlayerId, BanOrMute mute) {
    this.uuid = onlinePlayerId;
    this.reason = mute.getReason();
    this.shadow = mute.getVisibility() == PunishmentVisibility.SHADOW;
    this.expiresAt = determineExpirationTime(mute.getDuration());
    this.issuerId = Optional.ofNullable(mute.getIssuer()).map(CursoryPlayerInfo::getUuid).orElse(null);
    this.id = mute.getExternalId();
  }

  private static Long determineExpirationTime(Long duration) {
    long startTime = System.currentTimeMillis();
    return duration == null ? null : duration + startTime;
  }
}
