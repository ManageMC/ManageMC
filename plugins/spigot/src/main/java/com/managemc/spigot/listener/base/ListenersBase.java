package com.managemc.spigot.listener.base;

import com.managemc.plugins.logging.BukkitLogging;
import com.managemc.spigot.listener.base.util.EventHandlerRunner;
import com.managemc.spigot.listener.base.util.EventLogicHandler;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

public abstract class ListenersBase implements Listener {

  private final BukkitLogging logging;
  private final EventHandlerRunner eventHandlerRunner;

  public ListenersBase(BukkitLogging logging) {
    this.logging = logging;
    this.eventHandlerRunner = new EventHandlerRunner(logging);
  }

  protected <T extends Event> void runEvent(T event, EventLogicHandler<T> handler) {
    try {
      eventHandlerRunner.runCustomEventLogic(event, handler);
    } catch (RuntimeException e) {
      logging.logStackTrace(e);
    }
  }
}
