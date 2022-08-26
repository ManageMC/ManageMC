package com.managemc.spigot.task;

import com.managemc.plugins.bukkit.BukkitWrapper;
import com.managemc.plugins.logging.BukkitLogging;
import com.managemc.spigot.service.HeartbeatService;

import java.util.Timer;

public class PeriodicHeartbeatSender {

  // it is critically important that this be a divisor of 60000!
  public static final long HEARTBEAT_PERIOD_MILLIS = 20_000;

  private final Timer timer;
  private final PeriodicHeartbeatTask heartbeatTask;

  public PeriodicHeartbeatSender(BukkitLogging logging, BukkitWrapper bukkitWrapper, HeartbeatService heartbeatService) {
    this.timer = new Timer();
    this.heartbeatTask = new PeriodicHeartbeatTask(logging, bukkitWrapper, heartbeatService);
  }

  public void start() {
    long timeToStart = currentTime();
    if (timeToStart % HEARTBEAT_PERIOD_MILLIS != 0) {
      timeToStart = timeToStart + (HEARTBEAT_PERIOD_MILLIS - timeToStart % HEARTBEAT_PERIOD_MILLIS);
    }
    start(timeToStart - currentTime());
  }

  void startImmediately() {
    start(0);
  }

  long currentTime() {
    return System.currentTimeMillis();
  }

  void start(long delay) {
    timer.scheduleAtFixedRate(heartbeatTask, delay, HEARTBEAT_PERIOD_MILLIS);
  }

  /*
      Canceling the timer prevents any tasks from being scheduled in it without interfering with
      any active tasks. Canceling the heartbeat task cancels the task, but does not interrupt an
      active task execution, which will run to completion.

      Thus, it is possible, with relatively low probability, for the plugin to send the final
      heartbeat before the last periodic heartbeat finishes. If this becomes a real issue for
      customers, we can add a check to synchronously wait for the periodic heartbeat to finish
      before calling the final heartbeat. But we suspect that nobody will care.
  */
  public void stop() {
    timer.cancel();
    heartbeatTask.cancel();
  }
}
