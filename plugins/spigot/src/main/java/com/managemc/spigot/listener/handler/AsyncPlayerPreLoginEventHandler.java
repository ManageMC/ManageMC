package com.managemc.spigot.listener.handler;

import com.managemc.api.ApiException;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.listener.base.util.EventLogicHandler;
import com.managemc.spigot.state.model.MuteData;
import com.managemc.spigot.util.KickMessages;
import org.bukkit.ChatColor;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.openapitools.client.model.PunishmentVisibility;
import org.openapitools.client.model.ServerJoinEventResponse;

public class AsyncPlayerPreLoginEventHandler implements EventLogicHandler<AsyncPlayerPreLoginEvent> {

  static final String COULD_NOT_FETCH_PROFILE = ChatColor.RED +
      "We encountered an error fetching your profile. Please report this if it persists.";
  static final String SPECIAL_MESSAGE = "Failed to establish server connection";

  private static final AsyncPlayerPreLoginEvent.Result KICK_BANNED_RESULT = AsyncPlayerPreLoginEvent.Result.KICK_BANNED;
  private static final AsyncPlayerPreLoginEvent.Result KICK_OTHER_RESULT = AsyncPlayerPreLoginEvent.Result.KICK_OTHER;

  private final SpigotPluginConfig config;
  private final Sleeper sleeper;

  public AsyncPlayerPreLoginEventHandler(SpigotPluginConfig config) {
    this(config, new DefaultSleeper());
  }

  public AsyncPlayerPreLoginEventHandler(SpigotPluginConfig config, Sleeper sleeper) {
    this.config = config;
    this.sleeper = sleeper;
  }

  @Override
  public void handleEventLogic(AsyncPlayerPreLoginEvent event) {
    try {
      ServerJoinEventResponse loginResponse = config.getLoginLogoutService().onLogin(
          event.getName(),
          event.getUniqueId(),
          event.getAddress()
      );

      if (loginResponse.getBan() != null) {
        if (loginResponse.getBan().getVisibility() == PunishmentVisibility.SHADOW) {
          // wait on purpose for a random amount of time, kick for fake reason
          sleeper.sleep();
          event.disallow(KICK_OTHER_RESULT, SPECIAL_MESSAGE);
        } else {
          event.disallow(KICK_BANNED_RESULT, KickMessages.banMessage(loginResponse.getBan()));
        }
        return;
      }

      if (loginResponse.getWarning() != null) {
        event.disallow(KICK_OTHER_RESULT, KickMessages.warningMessage(loginResponse.getWarning()));
        return;
      }

      if (loginResponse.getMute() != null) {
        MuteData muteData = new MuteData(event.getUniqueId(), loginResponse.getMute());
        config.getMuteManager().setMuted(muteData);
      }

      config.getPlayerData().registerPlayer(event.getUniqueId(), loginResponse);
      config.getPermissionsManager().registerIntentToAssignPermissions(event.getUniqueId(), loginResponse.getPermissions());

    } catch (ApiException e) {
      config.getLogging().logStackTrace(e);
      event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, COULD_NOT_FETCH_PROFILE);
    } catch (InterruptedException e) {
      config.getLogging().logStackTrace(e);
      event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, SPECIAL_MESSAGE);
    }
  }

  public interface Sleeper {
    void sleep() throws InterruptedException;
  }

  private static class DefaultSleeper implements Sleeper {

    private static final long TEN_SECONDS = 1000 * 10;
    private static final long TWENTY_SECONDS = TEN_SECONDS * 2;

    @Override
    public void sleep() throws InterruptedException {
      double randomSleepTime = (Math.random() * TWENTY_SECONDS);
      Thread.sleep(TEN_SECONDS + (int) randomSleepTime);
    }
  }
}
