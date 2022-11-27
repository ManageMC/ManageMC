package com.managemc.spigot.service;

import com.managemc.api.ApiException;
import com.managemc.api.wrapper.ClientProvider;
import com.managemc.plugins.command.AbortCommand;
import com.managemc.spigot.command.util.CommandAssertions;
import org.bukkit.entity.Player;
import org.openapitools.client.model.PlayerWatchList;

import java.util.UUID;

public class WatchlistService {

  public static final String PLAYER_NOT_FOUND = "This player does not exist in our database yet. " +
      "Please use the following command to add it:\n/mmc players create";
  public static final String WATCHLIST_TOO_BIG = "Your watchlist has reached its max size";

  private final ClientProvider provider;

  public WatchlistService(ClientProvider provider) {
    this.provider = provider;
  }

  public PlayerWatchList getWatchlist(Player sender) throws ApiException {
    try {
      return provider.player(sender.getUniqueId()).getPlayersApi().fetchPlayerWatchlist();
    } catch (ApiException e) {
      if (e.getCode() == 403) {
        throw AbortCommand.withoutUsageMessage(CommandAssertions.NO_PERMS_MESSAGE);
      }
      throw e;
    }
  }

  public PlayerWatchList addToWatchlist(Player sender, UUID playerId) throws ApiException {
    try {
      return provider.player(sender.getUniqueId()).getPlayersApi().watchPlayer(playerId.toString());
    } catch (ApiException e) {
      switch (e.getCode()) {
        case 422:
          if (e.getResponseBody().contains("Player not found")) {
            throw AbortCommand.withoutUsageMessage(PLAYER_NOT_FOUND);
          } else if (e.getResponseBody().contains("Watchlist already at max size")) {
            throw AbortCommand.withoutUsageMessage(WATCHLIST_TOO_BIG);
          }
          break;
        case 403:
          throw AbortCommand.withoutUsageMessage(CommandAssertions.NO_PERMS_MESSAGE);
      }
      throw e;
    }
  }

  public PlayerWatchList removeFromWatchlist(Player sender, UUID playerId) throws ApiException {
    try {
      return provider.player(sender.getUniqueId()).getPlayersApi().unwatchPlayer(playerId.toString());
    } catch (ApiException e) {
      switch (e.getCode()) {
        case 422:
          throw AbortCommand.withoutUsageMessage(PLAYER_NOT_FOUND);
        case 403:
          throw AbortCommand.withoutUsageMessage(CommandAssertions.NO_PERMS_MESSAGE);
      }
      throw e;
    }
  }
}
