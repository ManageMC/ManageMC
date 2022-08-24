package com.managemc.spigot.service;

import com.managemc.api.ApiException;
import com.managemc.spigot.config.SpigotPluginConfig;
import org.openapitools.client.model.ImportPlayersInput;
import org.openapitools.client.model.ImportablePlayer;

import java.util.UUID;

public class CreatePlayerService {

  private final SpigotPluginConfig config;

  public CreatePlayerService(SpigotPluginConfig config) {
    this.config = config;
  }

  public void createPlayer(String username, UUID uuid) throws ApiException {
    ImportPlayersInput importPlayersInput = new ImportPlayersInput()
        .addPlayersItem(new ImportablePlayer().uuid(uuid).username(username));
    config.getClientProvider().externalServer().getPlayersApi().importPlayers(importPlayersInput);
  }
}
