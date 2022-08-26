package com.managemc.spigot.listener.handler;

import com.managemc.spigot.config.model.PermissionsManager;
import com.managemc.spigot.testutil.TestBase;
import com.managemc.spigot.testutil.TestConstants;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.UUID;

public class PlayerQuitEventHandlerTest extends TestBase {

  @Mock
  private Player player;
  @Mock
  private PlayerQuitEvent event;
  @Mock
  private PermissionsManager permissionsManager;

  private PlayerQuitEventHandler handler;

  @Before
  public void setup() {
    handler = new PlayerQuitEventHandler(config);
    Mockito.when(event.getPlayer()).thenReturn(player);

    config.setPermissionsManager(permissionsManager);
  }

  @Test
  public void success() {
    Mockito.when(player.getUniqueId()).thenReturn(TestConstants.JACOB_UUID);
    config.getPlayerData().registerPlayer(TestConstants.JACOB_UUID, TestConstants.JACOB_ID);
    awaitWebServiceResponse(() -> handler.handleEventLogic(event));

    Mockito.verify(config.getPermissionsManager()).clearPermissions(player);
    Mockito.verify(config.getLogging(), Mockito.never()).logStackTrace(Mockito.any());
  }

  @Test
  public void apiException() {
    UUID uuid = UUID.randomUUID();
    Mockito.when(player.getUniqueId()).thenReturn(uuid);
    config.getPlayerData().registerPlayer(uuid, 0);
    awaitWebServiceResponse(() -> handler.handleEventLogic(event));

    // we should clean up permissions regardless of whether the API barfs
    Mockito.verify(config.getPermissionsManager()).clearPermissions(player);
    Mockito.verify(config.getLogging(), Mockito.times(1)).logStackTrace(Mockito.any());
  }
}
