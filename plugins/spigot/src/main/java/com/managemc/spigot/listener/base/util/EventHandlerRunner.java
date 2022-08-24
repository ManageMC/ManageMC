package com.managemc.spigot.listener.base.util;

import com.managemc.plugins.logging.BukkitLogging;
import org.bukkit.event.Event;

public class EventHandlerRunner {

  private final BukkitLogging logging;

  public EventHandlerRunner(BukkitLogging logging) {
    this.logging = logging;
  }

  public <E extends Event> void runCustomEventLogic(E event, EventLogicHandler<E> handler) {
    try {
      handler.handleEventLogic(event);
    } catch (Exception e) {
      logging.logStackTrace(e);
    }
  }
}
