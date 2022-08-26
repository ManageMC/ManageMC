package com.managemc.spigot.listener.handler;

import com.google.common.collect.Sets;
import com.managemc.spigot.testutil.TestBase;
import com.managemc.spigot.testutil.TestConstants;
import com.managemc.spigot.util.chat.formatter.MuteDataFormatter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class AsyncPlayerChatEventHandlerTest extends TestBase {

  private static final long ONE_HOUR_MILLIS = 1000L * 60 * 60;
  private static final String NULL = ChatColor.GRAY + "null";

  private static final VerificationMode NEVER = Mockito.never();
  private static final VerificationMode ONCE = Mockito.times(1);

  private AsyncPlayerChatEventHandler handler;

  private Player jacob;
  private AsyncPlayerChatEvent event;
  private Set<Player> recipients;

  private boolean shadow;
  private Long expiresAt;
  private String reason;

  @Before
  public void setup() {
    handler = new AsyncPlayerChatEventHandler(config);

    jacob = Mockito.mock(Player.class);
    Mockito.when(jacob.getUniqueId()).thenReturn(TestConstants.JACOB_UUID);
    event = Mockito.mock(AsyncPlayerChatEvent.class);
    Mockito.when(event.getPlayer()).thenReturn(jacob);
    recipients = Sets.newHashSet(jacob, Mockito.mock(Player.class));
    Mockito.when(event.getRecipients()).thenReturn(recipients);

    shadow = false;
    expiresAt = null;
    reason = null;
  }

  @Test
  public void playerNotMuted() {
    issueCommand();
    Mockito.verify(event, NEVER).setCancelled(ArgumentMatchers.anyBoolean());
    assertEquals(2, recipients.size());
  }

  @Test
  public void shadowMute() {
    shadow = true;
    issueMute();
    issueCommand();
    Mockito.verify(jacob, NEVER).sendMessage(Mockito.anyString());
    Mockito.verify(event, NEVER).setCancelled(ArgumentMatchers.anyBoolean());
    assertEquals(Collections.singleton(jacob), recipients);
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
    reason = "fooling around";
    issueMute();
    issueCommand();
    Mockito.verify(jacob, ONCE).sendMessage(Mockito.contains(MuteDataFormatter.MUTED_HEADER));
    Mockito.verify(jacob, ONCE).sendMessage(Mockito.contains("reason: " + ChatColor.BLUE + "fooling around"));
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
    reason = "fooling around";
    expiresAt = System.currentTimeMillis() + ONE_HOUR_MILLIS + 999;
    issueMute();
    issueCommand();
    Mockito.verify(jacob, ONCE).sendMessage(Mockito.contains(MuteDataFormatter.MUTED_HEADER));
    Mockito.verify(jacob, ONCE).sendMessage(Mockito.contains("reason: " + ChatColor.BLUE + "fooling around"));
    Mockito.verify(jacob, ONCE).sendMessage(Mockito.contains("expires: " + ChatColor.YELLOW + "1 hour from now"));
    Mockito.verify(event, ONCE).setCancelled(true);
  }

  private void issueMute() {
    issueMute(shadow, reason, expiresAt);
  }

  private void issueCommand() {
    handler.handleEventLogic(event);
  }
}
