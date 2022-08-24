package com.managemc.spigot.listener.handler;

import com.managemc.spigot.config.model.PermissionsManager;
import com.managemc.spigot.testutil.TestBase;
import com.managemc.spigot.testutil.TestConstants;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.UUID;

public class PlayerKickEventHandlerTest extends TestBase {

  @Mock
  private PlayerKickEvent event;
  @Mock
  private PermissionsManager permissionsManager;

  private Player player;
  private PlayerKickEventHandler handler;

  @Before
  public void setup() {
    handler = new PlayerKickEventHandler(config);
    player = newStubbedPlayer(TestConstants.JACOB_USERNAME, TestConstants.JACOB_UUID, true);
    Mockito.when(event.getPlayer()).thenReturn(player);

    config.setPermissionsManager(permissionsManager);
  }

  @Test
  public void success() {
    Mockito.when(player.getUniqueId()).thenReturn(TestConstants.JACOB_UUID);
    config.getPlayerData().registerPlayer(player.getUniqueId(), TestConstants.JACOB_ID);
    awaitWebServiceResponse(() -> handler.handleEventLogic(event), 2);

    String expectedLogMessage = String.format(PlayerKickEventHandler.LOG_MESSAGE, TestConstants.JACOB_USERNAME);
    Mockito.verify(config.getLogging()).logInfo(expectedLogMessage);
    Mockito.verify(config.getLogging(), Mockito.never()).logWarning(Mockito.any());
    Mockito.verify(config.getLogging(), Mockito.never()).logStackTrace(Mockito.any());
    Mockito.verify(permissionsManager).clearPermissions(player);
  }

  @Test
  public void failure_playerNotFound() {
    Mockito.when(player.getUniqueId()).thenReturn(UUID.randomUUID());
    config.getPlayerData().registerPlayer(player.getUniqueId(), TestConstants.JACOB_ID);
    awaitWebServiceResponse(() -> handler.handleEventLogic(event), 2);

    Mockito.verify(config.getLogging(), Mockito.never()).logInfo(Mockito.any());
    Mockito.verify(config.getLogging(), Mockito.times(1))
        .logWarning(String.format(PlayerKickEventHandler.WARNING_MESSAGE, TestConstants.JACOB_USERNAME));
    Mockito.verify(config.getLogging(), Mockito.never()).logStackTrace(Mockito.any());
    Mockito.verify(permissionsManager).clearPermissions(player);
  }

  @Test
  public void failure_apiException_whenCreatingNote() {
    UUID invalidUuid = Mockito.mock(UUID.class);
    Mockito.when(invalidUuid.toString()).thenReturn("ayy lmao");
    Mockito.when(player.getUniqueId()).thenReturn(invalidUuid);
    config.getPlayerData().registerPlayer(player.getUniqueId(), TestConstants.JACOB_ID);
    awaitWebServiceResponse(() -> handler.handleEventLogic(event), 2);

    Mockito.verify(config.getLogging(), Mockito.never()).logInfo(Mockito.any());
    Mockito.verify(config.getLogging(), Mockito.never()).logWarning(Mockito.any());
    Mockito.verify(config.getLogging(), Mockito.times(1)).logStackTrace(Mockito.any());
    Mockito.verify(permissionsManager).clearPermissions(player);
  }

  @Test
  public void failure_apiException_whenRecordingLeaveEvent() {
    Mockito.when(player.getUniqueId()).thenReturn(TestConstants.JACOB_UUID);
    config.getPlayerData().registerPlayer(player.getUniqueId(), 0);
    awaitWebServiceResponse(() -> handler.handleEventLogic(event), 2);

    Mockito.verify(config.getLogging(), Mockito.times(1)).logInfo(Mockito.any());
    Mockito.verify(config.getLogging(), Mockito.never()).logWarning(Mockito.any());
    Mockito.verify(config.getLogging(), Mockito.times(1)).logStackTrace(Mockito.any());
    Mockito.verify(permissionsManager).clearPermissions(player);
  }
}
