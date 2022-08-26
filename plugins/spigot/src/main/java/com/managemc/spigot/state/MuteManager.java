package com.managemc.spigot.state;

import com.managemc.spigot.state.model.MuteData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MuteManager {

  private final Map<UUID, MuteData> activeMutes = new ConcurrentHashMap<>();

  /**
   * If the player is already muted, then the new mute may not override the old one.
   * <p>
   * Here are the rules, in order:
   * - new overrides old if the old one is expired
   * - new overrides old if the new one is a shadow mute and the old one is not
   * - new overrides old if it will last at least as long as the old one
   */
  public void setMuted(@Nonnull MuteData muteData) {
    MuteData existingMute = getMuteData(muteData.getUuid());
    if (shouldOverrideExistingMute(existingMute, muteData)) {
      activeMutes.put(muteData.getUuid(), muteData);
    }
  }

  public void removeMute(@Nonnull UUID uuid) {
    activeMutes.remove(uuid);
  }

  /**
   * Returns the UUIDs of players whose mutes were pardoned. Does NOT include
   * players whose mutes were shadow mutes. The consumer may use this information
   * to send a message to each online player who may use chat freely once again.
   */
  public Set<UUID> removeMutes(Collection<String> ids) {
    Set<UUID> playersWhoseMutesWereRemoved = new HashSet<>();
    Set<String> idsSet = ids.stream().map(String::toUpperCase).collect(Collectors.toSet());

    Iterator<Map.Entry<UUID, MuteData>> iterator = activeMutes.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<UUID, MuteData> entry = iterator.next();
      UUID uuid = entry.getKey();
      MuteData muteData = entry.getValue();

      if (idsSet.contains(muteData.getId())) {
        if (!muteData.isShadow()) {
          playersWhoseMutesWereRemoved.add(uuid);
        }
        iterator.remove();
      }
    }

    return playersWhoseMutesWereRemoved;
  }

  @Nullable
  public MuteData getMuteData(UUID uuid) {
    MuteData muteData = activeMutes.get(uuid);
    if (muteData == null || muteData.getExpiresAt() == null) {
      return muteData;
    }
    if (muteData.getExpiresAt() < System.currentTimeMillis()) {
      removeMute(uuid);
      return null;
    }
    return muteData;
  }

  private boolean shouldOverrideExistingMute(MuteData existingMute, MuteData newMute) {
    if (existingMute == null) {
      return true;
    }

    if (existingMute.getExpiresAt() != null && existingMute.getExpiresAt() < System.currentTimeMillis()) {
      return true;
    }

    if (newMute.isShadow() && !existingMute.isShadow()) {
      return true;
    }

    if (newMute.getExpiresAt() == null) {
      return true;
    }

    if (existingMute.getExpiresAt() == null) {
      return false;
    }

    return newMute.getExpiresAt() >= existingMute.getExpiresAt();
  }
}
