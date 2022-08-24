package com.managemc.spigot.task;

import com.managemc.plugins.bukkit.BukkitWrapper;
import com.managemc.plugins.logging.BukkitLogging;
import com.managemc.spigot.service.HeartbeatService;

import java.util.TimerTask;

/**
 * this is a TimerTask instead of a BukkitRunnable because BukkitRunnable runs on
 * game ticks, which are subject to delay in the event of server performance issues.
 * TimerTask makes it much more likely that the requests will be sent exactly once
 * per defined interval.
 */
public class PeriodicHeartbeatTask extends TimerTask {

  static final int MAX_FAILURES_BEFORE_SHUTDOWN = 6;
  static final String REQUEST_FAILURE_MESSAGE = "Failed to send heartbeat to ManageMC. Since this might be temporary, the server will only shut down if this persists for more than two minutes.";
  static final String SHUTTING_DOWN_MESSAGE = "Shutting down due to repeated heartbeat failure";

  private final BukkitLogging logging;
  private final BukkitWrapper bukkitWrapper;
  private final HeartbeatService heartbeatService;
  private int consecutiveFailureCount = 0;

  public PeriodicHeartbeatTask(BukkitLogging logging, BukkitWrapper bukkitWrapper, HeartbeatService heartbeatService) {
    this.logging = logging;
    this.bukkitWrapper = bukkitWrapper;
    this.heartbeatService = heartbeatService;
  }

  /*
      While the plugin is running, we make periodic heartbeat requests to track server uptime
      and catch when the plugin version becomes out of date, among other things
  */
  @Override
  public void run() {
    try {
      heartbeatService.emitHeartbeat();
      consecutiveFailureCount = 0;
    } catch (Exception e) {
      consecutiveFailureCount++;
      logging.logStackTrace(e);
      if (consecutiveFailureCount <= MAX_FAILURES_BEFORE_SHUTDOWN) {
        logging.logWarning(REQUEST_FAILURE_MESSAGE);
      } else {
        logging.logSevere(SHUTTING_DOWN_MESSAGE);
        bukkitWrapper.shutdown();
      }
    }
  }
}
