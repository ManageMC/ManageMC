package com.managemc.spigot.service;

import com.managemc.api.ApiException;
import com.managemc.api.wrapper.ClientProvider;
import com.managemc.spigot.command.util.CommandValidationException;
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
    return provider.player(sender.getUniqueId()).getPlayersApi().fetchPlayerWatchlist();
  }

  public PlayerWatchList addToWatchlist(Player sender, UUID playerId) throws ApiException {
    try {
      return provider.player(sender.getUniqueId()).getPlayersApi().watchPlayer(playerId.toString());
    } catch (ApiException e) {
      if (e.getCode() == 422) {
        if (e.getResponseBody().contains("Player not found")) {
          throw new CommandValidationException(PLAYER_NOT_FOUND, false);
        } else if (e.getResponseBody().contains("Watchlist already at max size")) {
          throw new CommandValidationException(WATCHLIST_TOO_BIG, false);
        }
      }
      throw e;
    }
  }

  public PlayerWatchList removeFromWatchlist(Player sender, UUID playerId) throws ApiException {
    try {
      return provider.player(sender.getUniqueId()).getPlayersApi().unwatchPlayer(playerId.toString());
    } catch (ApiException e) {
      if (e.getCode() == 422) {
        throw new CommandValidationException(PLAYER_NOT_FOUND, false);
      }
      throw e;
    }
  }
}
