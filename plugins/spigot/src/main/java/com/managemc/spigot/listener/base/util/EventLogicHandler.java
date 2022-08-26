package com.managemc.spigot.listener.base.util;

import org.bukkit.event.Event;

public interface EventLogicHandler<E extends Event> {

  void handleEventLogic(E event);
}
