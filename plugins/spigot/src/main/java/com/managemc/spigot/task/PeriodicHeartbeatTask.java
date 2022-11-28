package com.managemc.spigot.task;

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

  static final int MAX_TRANSIENT_FAILURES = 3;
  static final String TRANSIENT_FAILURE_MESSAGE = "Failed to send heartbeat to ManageMC. Next time, we will attempt to catch up.";
  static final String PERSISTENT_FAILURE_MESSAGE = "Persistent heartbeat failure. Printing stack trace now.";

  private final BukkitLogging logging;
  private final HeartbeatService heartbeatService;
  private int consecutiveFailureCount = 0;

  public PeriodicHeartbeatTask(BukkitLogging logging, HeartbeatService heartbeatService) {
    this.logging = logging;
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
      if (consecutiveFailureCount <= MAX_TRANSIENT_FAILURES) {
        logging.logWarning(TRANSIENT_FAILURE_MESSAGE);
      } else {
        logging.logSevere(PERSISTENT_FAILURE_MESSAGE);
        logging.logStackTrace(e);
      }
    }
  }
}
