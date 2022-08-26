package com.managemc.spigot.listener.handler;

import com.managemc.spigot.config.model.PermissionsManager;
import com.managemc.spigot.testutil.TestBase;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class PlayerLoginEventHandlerTest extends TestBase {

  @Mock
  private PlayerLoginEvent event;
  @Mock
  private Player player;
  @Mock
  private PermissionsManager permissionsManager;

  private PlayerLoginEventHandler handler;

  @Before
  public void setup() {
    handler = new PlayerLoginEventHandler(config);
    Mockito.when(event.getPlayer()).thenReturn(player);

    config.setPermissionsManager(permissionsManager);
  }

  @Test
  public void success() {
    handler.handleEventLogic(event);

    Mockito.verify(permissionsManager).assignPermissions(player);
  }
}
