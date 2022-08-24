package com.managemc.spigot.state;

import org.openapitools.client.model.ServerJoinEventResponse;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerData {

  private final Map<UUID, Long> playerIdsByUuid = new ConcurrentHashMap<>();

  public void registerPlayer(UUID uuid, ServerJoinEventResponse serverJoinResponse) {
    registerPlayer(uuid, serverJoinResponse.getPlayerId());
  }

  public void registerPlayer(UUID uuid, long id) {
    playerIdsByUuid.put(uuid, id);
  }

  public Long getPlayerId(UUID uuid) {
    return playerIdsByUuid.get(uuid);
  }

  public void clear() {
    playerIdsByUuid.clear();
  }
}
