package com.managemc.spigot.listener.handler;

import com.managemc.spigot.service.RemoteConfigService;
import com.managemc.spigot.service.model.RemoteConfig;
import com.managemc.spigot.testutil.TestBase;
import com.managemc.spigot.testutil.TestConstants;
import com.managemc.spigot.util.chat.formatter.MuteDataFormatter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

import java.util.Collections;
import java.util.List;

public class PlayerCommandPreprocessEventHandlerTest extends TestBase {

  private static final long ONE_HOUR_MILLIS = 1000L * 60 * 60;
  private static final List<String> BLOCKED_COMMANDS = Collections.singletonList("tell");
  private static final String NULL = ChatColor.GRAY + "null";

  private static final VerificationMode NEVER = Mockito.never();
  private static final VerificationMode ONCE = Mockito.times(1);

  private PlayerCommandPreprocessEventHandler handler;
  private Player jacob;
  private PlayerCommandPreprocessEvent event;

  private String command;
  private boolean shadow;
  private Long expiresAt;
  private String reason;

  @Before
  public void setup() {
    RemoteConfig remoteConfig = Mockito.mock(RemoteConfig.class);
    Mockito.when(remoteConfig.getBlockedCommandsForMutedPlayers()).thenReturn(BLOCKED_COMMANDS);
    RemoteConfigService remoteConfigService = Mockito.mock(RemoteConfigService.class);
    Mockito.when(remoteConfigService.getRemoteConfig()).thenReturn(remoteConfig);
    config.setRemoteConfigService(remoteConfigService);

    handler = new PlayerCommandPreprocessEventHandler(config);
    jacob = newStubbedPlayer(TestConstants.JACOB_USERNAME, TestConstants.JACOB_UUID, true);
    event = Mockito.mock(PlayerCommandPreprocessEvent.class);
    Mockito.when(event.getPlayer()).thenReturn(jacob);

    command = "/tell ayy lmao";
    shadow = false;
    expiresAt = null;
    reason = null;
  }

  @Test
  public void irrelevantCommand() {
    command = "/ping";
    issueMute();
    issueCommand();
    Mockito.verify(event, NEVER).setCancelled(ArgumentMatchers.anyBoolean());
  }

  @Test
  public void commandWithoutSlashSomehow() {
    command = "tell";
    issueMute();
    issueCommand();
    Mockito.verify(event, NEVER).setCancelled(ArgumentMatchers.anyBoolean());
  }

  @Test
  public void differentCommandWithSameStart() {
    command = "/telly ayy lmao";
    issueMute();
    issueCommand();
    Mockito.verify(event, NEVER).setCancelled(ArgumentMatchers.anyBoolean());
  }

  @Test
  public void commandEqualsExactly() {
    command = "/tell";
    issueMute();
    issueCommand();
    Mockito.verify(event, ONCE).setCancelled(true);
  }

  @Test
  public void playerNotMuted() {
    issueCommand();
    Mockito.verify(event, NEVER).setCancelled(ArgumentMatchers.anyBoolean());
  }

  @Test
  public void shadowMute() {
    shadow = true;
    issueMute();
    issueCommand();
    Mockito.verify(jacob, NEVER).sendMessage((String) ArgumentMatchers.any());
    Mockito.verify(event, ONCE).setCancelled(true);
  }

  @Test
  public void permanentMute() {
    issueMute();
    issueCommand();
    Mockito.verify(jacob, ONCE).sendMessage(Mockito.contains(MuteDataFormatter.MUTED_HEADER));
    Mockito.verify(jacob, ONCE).sendMessage(Mockito.contains("reason: " + NULL));
    Mockito.verify(jacob, ONCE).sendMessage(Mockito.contains("expires: " + NULL));
    Mockito.verify(event, ONCE).setCancelled(true);
  }

  @Test
  public void permanentMuteWithReason() {
    reason = "messing around";
    issueMute();
    issueCommand();
    Mockito.verify(jacob, ONCE).sendMessage(Mockito.contains(MuteDataFormatter.MUTED_HEADER));
    Mockito.verify(jacob, ONCE).sendMessage(Mockito.contains("reason: " + ChatColor.BLUE + "messing around"));
    Mockito.verify(jacob, ONCE).sendMessage(Mockito.contains("expires: " + NULL));
    Mockito.verify(event, ONCE).setCancelled(true);
  }

  @Test
  public void tempMute() {
    expiresAt = System.currentTimeMillis() + ONE_HOUR_MILLIS + 999;
    issueMute();
    issueCommand();
    Mockito.verify(jacob, ONCE).sendMessage(Mockito.contains(MuteDataFormatter.MUTED_HEADER));
    Mockito.verify(jacob, ONCE).sendMessage(Mockito.contains("reason: " + NULL));
    Mockito.verify(jacob, ONCE).sendMessage(Mockito.contains("expires: " + ChatColor.YELLOW + "1 hour from now"));
    Mockito.verify(event, ONCE).setCancelled(true);
  }

  @Test
  public void tempMuteWithReason() {
    reason = "messing around";
    expiresAt = System.currentTimeMillis() + ONE_HOUR_MILLIS + 999;
    issueMute();
    issueCommand();
    Mockito.verify(jacob, ONCE).sendMessage(Mockito.contains(MuteDataFormatter.MUTED_HEADER));
    Mockito.verify(jacob, ONCE).sendMessage(Mockito.contains("reason: " + ChatColor.BLUE + "messing around"));
    Mockito.verify(jacob, ONCE).sendMessage(Mockito.contains("expires: " + ChatColor.YELLOW + "1 hour from now"));
    Mockito.verify(event, ONCE).setCancelled(true);
  }

  private void issueMute() {
    issueMute(shadow, reason, expiresAt);
  }

  private void issueCommand() {
    Mockito.when(event.getMessage()).thenReturn(command);
    handler.handleEventLogic(event);
  }
}
