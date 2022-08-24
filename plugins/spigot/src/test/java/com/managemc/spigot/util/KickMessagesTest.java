package com.managemc.spigot.util;

import org.bukkit.ChatColor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openapitools.client.model.BanOrMute;
import org.openapitools.client.model.IpBan;
import org.openapitools.client.model.PunishmentVisibility;
import org.openapitools.client.model.Warning;

@RunWith(MockitoJUnitRunner.Silent.class)
public class KickMessagesTest {

  private static final String BAN_FIRST_LINE = ChatColor.YELLOW + "You have been banned from this server." + ChatColor.RESET + "\n";
  private static final String WARNING_FIRST_LINE = ChatColor.YELLOW + "You have received a formal warning on this server." + ChatColor.RESET + "\n";
  private static final String WARNING_LAST_LINE = "\n\n" + ChatColor.YELLOW + "You must acknowledge the warning on ManageMC to resume playing: https://managemc.com/warnings" + ChatColor.RESET;

  @Mock
  private BanOrMute banOrMute;
  @Mock
  private IpBan ipBan;
  @Mock
  private Warning warning;

  private long issuedAt;
  private Long duration;
  private String reason;
  private PunishmentVisibility visibility;

  @Before
  public void setup() {
    issuedAt = System.currentTimeMillis() - TimeHelper.ONE_DAY * 7;
    duration = TimeHelper.ONE_DAY * 7 + TimeHelper.ONE_HOUR * 4 + TimeHelper.ONE_MINUTE * 35;
    reason = "bumptious behavior";
    visibility = PunishmentVisibility.PUBLIC;
  }

  @Test
  public void banKickMessage() {
    Assert.assertEquals(
        BAN_FIRST_LINE
            + line("reason: bumptious behavior")
            + line("expires: " + TimeHelper.formatTimestamp(issuedAt + duration) + " (5 hours from now)"),
        ban()
    );
  }

  @Test
  public void banKickMessage_permanent() {
    duration = null;

    Assert.assertEquals(
        BAN_FIRST_LINE
            + line("reason: bumptious behavior")
            + line("expires: never"),
        ban()
    );
  }

  @Test
  public void banKickMessage_noReason() {
    reason = null;

    Assert.assertEquals(
        BAN_FIRST_LINE
            + line("reason: ")
            + line("expires: " + TimeHelper.formatTimestamp(issuedAt + duration) + " (5 hours from now)"),
        ban()
    );
  }

  @Test
  public void banKickMessage_shadow() {
    visibility = PunishmentVisibility.SHADOW;

    Assert.assertEquals("Server closed", ban());
  }

  @Test
  public void banKickMessage_expiresSecondsFromNow() {
    duration = TimeHelper.ONE_DAY * 7 + TimeHelper.ONE_SECOND * 10;

    Assert.assertEquals(
        BAN_FIRST_LINE
            + line("reason: bumptious behavior")
            + line("expires: " + TimeHelper.formatTimestamp(issuedAt + duration) + " (moments from now)"),
        ban()
    );
  }

  @Test
  public void banKickMessage_expiresMinutesFromNow() {
    duration = TimeHelper.ONE_DAY * 7 + TimeHelper.ONE_MINUTE * 8;

    Assert.assertEquals(
        BAN_FIRST_LINE
            + line("reason: bumptious behavior")
            + line("expires: " + TimeHelper.formatTimestamp(issuedAt + duration) + " (8 minutes from now)"),
        ban()
    );
  }

  @Test
  public void ipBanKickMessage() {
    Assert.assertEquals(
        BAN_FIRST_LINE
            + line("reason: bumptious behavior")
            + line("expires: " + TimeHelper.formatTimestamp(issuedAt + duration) + " (5 hours from now)"),
        ipBan()
    );
  }

  @Test
  public void ipBanKickMessage_permanent() {
    duration = null;

    Assert.assertEquals(
        BAN_FIRST_LINE
            + line("reason: bumptious behavior")
            + line("expires: never"),
        ipBan()
    );
  }

  @Test
  public void ipBanKickMessage_noReason() {
    reason = null;

    Assert.assertEquals(
        BAN_FIRST_LINE
            + line("reason: ")
            + line("expires: " + TimeHelper.formatTimestamp(issuedAt + duration) + " (5 hours from now)"),
        ipBan()
    );
  }

  @Test
  public void ipBanKickMessage_shadow() {
    visibility = PunishmentVisibility.SHADOW;

    Assert.assertEquals("Server closed", ipBan());
  }

  @Test
  public void ipBanKickMessage_expiresSecondsFromNow() {
    duration = TimeHelper.ONE_DAY * 7 + TimeHelper.ONE_SECOND * 10;

    Assert.assertEquals(
        BAN_FIRST_LINE
            + line("reason: bumptious behavior")
            + line("expires: " + TimeHelper.formatTimestamp(issuedAt + duration) + " (moments from now)"),
        ipBan()
    );
  }

  @Test
  public void ipBanKickMessage_expiresMinutesFromNow() {
    duration = TimeHelper.ONE_DAY * 7 + TimeHelper.ONE_MINUTE * 8;

    Assert.assertEquals(
        BAN_FIRST_LINE
            + line("reason: bumptious behavior")
            + line("expires: " + TimeHelper.formatTimestamp(issuedAt + duration) + " (8 minutes from now)"),
        ipBan()
    );
  }

  @Test
  public void warningKickMessage() {
    Assert.assertEquals(
        WARNING_FIRST_LINE
            + line("reason: bumptious behavior")
            + WARNING_LAST_LINE,
        warning()
    );
  }

  @Test
  public void warningKickMessage_noReason() {
    reason = null;

    Assert.assertEquals(
        WARNING_FIRST_LINE
            + line("reason: ")
            + WARNING_LAST_LINE,
        warning()
    );
  }


  private String ban() {
    Mockito.when(banOrMute.getIssuedAt()).thenReturn(issuedAt);
    Mockito.when(banOrMute.getDuration()).thenReturn(duration);
    Mockito.when(banOrMute.getReason()).thenReturn(reason);
    Mockito.when(banOrMute.getVisibility()).thenReturn(visibility);

    return KickMessages.banMessage(banOrMute);
  }

  private String ipBan() {
    Mockito.when(ipBan.getIssuedAt()).thenReturn(issuedAt);
    Mockito.when(ipBan.getDuration()).thenReturn(duration);
    Mockito.when(ipBan.getReason()).thenReturn(reason);
    Mockito.when(ipBan.getVisibility()).thenReturn(visibility);

    return KickMessages.ipBanMessage(ipBan);
  }

  private String warning() {
    Mockito.when(warning.getReason()).thenReturn(reason);

    return KickMessages.warningMessage(warning);
  }

  private String line(String text) {
    return "\n" + ChatColor.YELLOW + text + ChatColor.RESET;
  }
}
