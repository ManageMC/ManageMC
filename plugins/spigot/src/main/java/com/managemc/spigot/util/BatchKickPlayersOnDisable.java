package com.managemc.spigot.util;

import com.managemc.api.ApiException;
import com.managemc.plugins.bukkit.BukkitWrapper;
import com.managemc.plugins.logging.BukkitLogging;
import com.managemc.spigot.service.LoginLogoutService;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class BatchKickPlayersOnDisable {

  private static final String RELOAD_MESSAGE = "Server is reloading";
  private static final int NUM_THREADS = 10;

  private final BukkitLogging logging;
  private final BukkitWrapper bukkitWrapper;
  private final LoginLogoutService service;

  public BatchKickPlayersOnDisable(BukkitLogging logging, BukkitWrapper bukkitWrapper, LoginLogoutService service) {
    this.logging = logging;
    this.bukkitWrapper = bukkitWrapper;
    this.service = service;
  }

  /**
   * Kicking the players during onDisable, as we are doing here, does not
   * trigger a PlayerKickEvent, which means that without this method, we
   * would never record the fact that the players left the server when
   * someone uses the /reload command. This uses a divide-and-conquer
   * approach across multiple threads.
   */
  public void run() {
    ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);

    List<Callable<Object>> todo = bukkitWrapper.getOnlinePlayers().stream()
        .peek(player -> player.kickPlayer(RELOAD_MESSAGE))
        .map((player) -> Executors.callable(logoutTaskForPlayer(player)))
        .collect(Collectors.toList());

    if (!todo.isEmpty()) {
      try {
        executorService.invokeAll(todo);
        executorService.shutdownNow();
      } catch (InterruptedException e) {
        logging.logStackTrace(e);
      }
    }
  }

  private Runnable logoutTaskForPlayer(Player player) {
    return () -> {
      try {
        service.onLogout(player.getUniqueId());
      } catch (ApiException e) {
        logging.logStackTrace(e);
      }
    };
  }
}
