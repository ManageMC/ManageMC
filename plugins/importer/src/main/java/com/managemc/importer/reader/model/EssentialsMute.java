package com.managemc.importer.reader.model;

import lombok.Getter;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class EssentialsMute {

  @Getter
  private final UUID uuid;
  @Getter
  private final String username;
  @Getter
  private final boolean muted;
  @Getter
  private final boolean npc;
  @Getter
  private final long mute;
  @Getter
  private final String reason;

  public EssentialsMute(UUID uuid, Map<String, Object> parsedFile) {
    this.uuid = uuid;
    this.username = Optional.ofNullable(parsedFile.get("last-account-name")).map(Object::toString).orElse(null);
    this.muted = Boolean.parseBoolean(parsedFile.get("muted").toString());
    this.npc = Boolean.parseBoolean(parsedFile.get("npc").toString());
    this.reason = Optional.ofNullable(parsedFile.get("mute-reason"))
        .map(Object::toString)
        .orElse(null);

    if (muted && !npc && parsedFile.get("timestamps") != null) {
      Map<String, Object> timestamps = (Map<String, Object>) parsedFile.get("timestamps");
      this.mute = Long.parseLong(timestamps.get("mute").toString());
    } else {
      this.mute = 0;
    }
  }
}
