package com.managemc.spigot.listener.handler;

import com.managemc.spigot.config.model.PermissionsManager;
import com.managemc.spigot.state.model.MuteData;
import com.managemc.spigot.testutil.TestBase;
import com.managemc.spigot.testutil.TestConstants;
import com.managemc.spigot.util.KickMessages;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

import java.net.InetAddress;
import java.util.UUID;

public class AsyncPlayerPreLoginEventHandlerTest extends TestBase {

  private static final VerificationMode NEVER = Mockito.never();

  private static final UUID BANNED_UUID = UUID.fromString("461691a7-87c4-4916-88b9-307ca7365858");
  private static final String BANNED_USERNAME = "Berkouwe";

  private static final UUID SHADOW_BANNED_UUID = UUID.fromString("f4ccdbee-4f59-4e15-9d8c-1728dd811802");
  public static final String SHADOW_BANNED_USERNAME = "Mod";

  private static final UUID WARNED_UUID = UUID.fromString("f93d99de-d61d-450e-9d7b-599ec55fce9b");
  private static final String WARNED_USERNAME = "RiverRat";

  private static final UUID MUTED_UUID = UUID.fromString("c78bba6b-3910-4ee2-b997-8356a6702011");
  private static final String MUTED_USERNAME = "Crow";
  private static final Long MUTED_ID = 14L;

  private static final String IP_ADDRESS = "5.5.5.5";

  @Mock
  public InetAddress inetAddress;
  @Mock
  public AsyncPlayerPreLoginEvent event;
  @Mock
  public AsyncPlayerPreLoginEventHandler.Sleeper sleeper;
  @Mock
  private PermissionsManager permissionsManager;

  private AsyncPlayerPreLoginEventHandler handler;

  @Before
  public void setup() {
    handler = new AsyncPlayerPreLoginEventHandler(config, sleeper);

    Mockito.when(inetAddress.getHostAddress()).thenReturn(IP_ADDRESS);
    Mockito.when(event.getAddress()).thenReturn(inetAddress);

    config.setPermissionsManager(permissionsManager);
  }

  @Test
  public void playerWithNoPunishments() {
    stubPlayer(TestConstants.JACOB_UUID, TestConstants.JACOB_USERNAME);
    handler.handleEventLogic(event);

    Assert.assertEquals((Long) TestConstants.JACOB_ID, config.getPlayerData().getPlayerId(TestConstants.JACOB_UUID));
    Assert.assertNull(config.getMuteManager().getMuteData(TestConstants.JACOB_UUID));
    assertAssignedPermissions(TestConstants.JACOB_UUID);
    Mockito.verify(event, NEVER).disallow(Mockito.any(AsyncPlayerPreLoginEvent.Result.class), Mockito.any());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Mockito.verifyNoInteractions(sleeper);
  }

  @Test
  public void bannedPlayer() {
    stubPlayer(BANNED_UUID, BANNED_USERNAME);
    handler.handleEventLogic(event);

    Assert.assertNull(config.getMuteManager().getMuteData(BANNED_UUID));
    Mockito.verifyNoInteractions(permissionsManager);
    Mockito.verify(event).disallow(Mockito.eq(AsyncPlayerPreLoginEvent.Result.KICK_BANNED), Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Mockito.verifyNoInteractions(sleeper);
  }

  @Test
  public void shadowBannedPlayer() throws InterruptedException {
    stubPlayer(SHADOW_BANNED_UUID, SHADOW_BANNED_USERNAME);
    handler.handleEventLogic(event);

    Assert.assertNull(config.getMuteManager().getMuteData(SHADOW_BANNED_UUID));
    Mockito.verifyNoInteractions(permissionsManager);
    Mockito.verify(event).disallow(Mockito.eq(AsyncPlayerPreLoginEvent.Result.KICK_OTHER), Mockito.eq(AsyncPlayerPreLoginEventHandler.SPECIAL_MESSAGE));
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Mockito.verify(sleeper, Mockito.times(1)).sleep();
  }

  @Test
  public void warnedPlayer() {
    stubPlayer(WARNED_UUID, WARNED_USERNAME);
    handler.handleEventLogic(event);

    Assert.assertNull(config.getMuteManager().getMuteData(BANNED_UUID));
    Mockito.verifyNoInteractions(permissionsManager);
    Mockito.verify(event).disallow(Mockito.eq(AsyncPlayerPreLoginEvent.Result.KICK_OTHER), Mockito.contains(KickMessages.WARNING_MESSAGE));
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Mockito.verifyNoInteractions(sleeper);
  }

  @Test
  public void mutedPlayer() {
    stubPlayer(MUTED_UUID, MUTED_USERNAME);
    handler.handleEventLogic(event);

    MuteData muteData = config.getMuteManager().getMuteData(MUTED_UUID);
    Assert.assertNotNull(muteData);
    Assert.assertNotNull(muteData.getReason());
    Assert.assertNull(muteData.getExpiresAt());
    Assert.assertEquals(MUTED_UUID, muteData.getUuid());

    Assert.assertEquals(MUTED_ID, config.getPlayerData().getPlayerId(MUTED_UUID));
    assertAssignedPermissions(MUTED_UUID);
    Mockito.verify(event, NEVER).disallow(Mockito.any(AsyncPlayerPreLoginEvent.Result.class), Mockito.any());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Mockito.verifyNoInteractions(sleeper);
  }

  @Test
  public void apiException() {
    Mockito.when(inetAddress.getHostAddress()).thenReturn("invalid IP");
    handler.handleEventLogic(event);

    Mockito.verifyNoInteractions(permissionsManager);
    Mockito.verify(config.getLogging()).logStackTrace(Mockito.any());
    Mockito.verify(event)
        .disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, AsyncPlayerPreLoginEventHandler.COULD_NOT_FETCH_PROFILE);
    Mockito.verifyNoInteractions(sleeper);
  }

  private void stubPlayer(UUID uuid, String username) {
    Mockito.when(event.getName()).thenReturn(username);
    Mockito.when(event.getUniqueId()).thenReturn(uuid);
  }

  private void assertAssignedPermissions(UUID uuid) {
    Mockito.verify(permissionsManager).registerIntentToAssignPermissions(Mockito.eq(uuid), Mockito.any());
  }
}
