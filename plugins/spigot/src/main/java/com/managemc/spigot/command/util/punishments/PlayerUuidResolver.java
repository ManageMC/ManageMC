package com.managemc.spigot.command.util.punishments;

import com.managemc.plugins.bukkit.BukkitWrapper;
import com.managemc.plugins.logging.BukkitLogging;
import com.managemc.plugins.util.UuidHyphenator;
import com.managemc.spigot.command.util.punishments.model.ResolvedPlayer;
import com.managemc.spigot.util.HttpRequestMaker;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PlayerUuidResolver {

  // https://wiki.vg/Mojang_API#Username_to_UUID
  private static final String URL = "https://api.mojang.com/users/profiles/minecraft/%s";
  private static final String RESPONSE_UUID_KEY = "id";

  public static final int OK_CODE = 200;
  public static final int NOT_FOUND_CODE = 204;
  public static final int RATE_LIMIT_EXCEEDED_CODE = 429;

  private final BukkitLogging logging;
  private final BukkitWrapper bukkitWrapper;
  private final HttpRequestMaker httpRequestMaker;

  public PlayerUuidResolver(BukkitLogging logging, BukkitWrapper bukkitWrapper) {
    this(logging, bukkitWrapper, new HttpRequestMaker());
  }

  public PlayerUuidResolver(BukkitLogging logging, BukkitWrapper bukkitWrapper, HttpRequestMaker httpRequestMaker) {
    this.logging = logging;
    this.bukkitWrapper = bukkitWrapper;
    this.httpRequestMaker = httpRequestMaker;
  }

  /**
   * Do not call this from the main server thread. It makes a blocking HTTP request
   * if it cannot find the player in Bukkit's recently-online cache.
   */
  @NonNull
  public ResolvedPlayer resolvePlayerPotentiallyBlocking(String username) {
    Player player = bukkitWrapper.getOnlineOrRecentlyOnlinePlayer(username);
    if (player != null) {
      return ResolvedPlayer.builder()
          .statusCode(ResolvedPlayer.Status.ONLINE_RECENTLY)
          .uuid(player.getUniqueId())
          .build();
    }

    try {
      String url = String.format(URL, username);
      HttpResponse<String> response = httpRequestMaker.get(url);

      switch (response.getStatus()) {
        case NOT_FOUND_CODE:
          return ResolvedPlayer.builder().statusCode(ResolvedPlayer.Status.HTTP_NOT_FOUND).build();
        case RATE_LIMIT_EXCEEDED_CODE:
          return ResolvedPlayer.builder().statusCode(ResolvedPlayer.Status.HTTP_RATE_LIMIT_EXCEEDED).build();
        case OK_CODE:
          JSONObject parsedBody = (JSONObject) new JSONParser().parse(response.getBody());
          String uuid = (String) parsedBody.get(RESPONSE_UUID_KEY);
          return ResolvedPlayer.builder()
              .statusCode(ResolvedPlayer.Status.HTTP_OK)
              .uuid(UuidHyphenator.toUUID(uuid))
              .build();
        default:
          String message = String
              .format("Unexpected HTTP code from Mojang API\n  code: %s\n  Player: %s", response.getStatus(), username);
          logging.logWarning(message);
          return ResolvedPlayer.builder().statusCode(ResolvedPlayer.Status.HTTP_UNEXPECTED).build();
      }
    } catch (UnirestException | ParseException e) {
      logging.logStackTrace(e);
      return ResolvedPlayer.builder().statusCode(ResolvedPlayer.Status.CLIENT_EXCEPTION).build();
    }
  }
}
