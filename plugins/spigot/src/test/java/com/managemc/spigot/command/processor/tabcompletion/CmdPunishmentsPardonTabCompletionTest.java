package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.spigot.testutil.TestBase;
import com.managemc.spigot.util.permissions.Permission;
import org.junit.Assert;
import org.junit.Test;

public class CmdPunishmentsPardonTabCompletionTest extends TestBase {

  @Test
  public void onTabComplete_noArgs() {
    Assert.assertEquals("[ban-, ip_ban-, mute-, warning-]", onTabComplete("punishments", "pardon", ""));
  }

  @Test
  public void onTabComplete_noArgs_withKnownIds() {
    config.getPardonablePunishmentData().addPunishment(sender, "MUTE-6");
    config.getPardonablePunishmentData().addPunishment(sender, "BAN-2");

    Assert.assertEquals("[ban-2, mute-6]", onTabComplete("punishments", "pardon", ""));
  }

  @Test
  public void onTabComplete_noArgs_trailingSpace() {
    Assert.assertEquals("[ban-, ip_ban-, mute-, warning-]", onTabComplete("punishments", "pardon", ""));
  }

  @Test
  public void onTabComplete_noArgs_trailingSpace_withKnownIds() {
    config.getPardonablePunishmentData().addPunishment(sender, "MUTE-6");
    config.getPardonablePunishmentData().addPunishment(sender, "BAN-2");

    Assert.assertEquals("[ban-2, mute-6]", onTabComplete("punishments", "pardon", ""));
  }

  @Test
  public void onTabComplete_oneArg_fullMatch() {
    Assert.assertEquals("[ban-]", onTabComplete("punishments", "pardon", "ban-"));
  }

  @Test
  public void onTabComplete_oneArg_fullMatch_differentCase() {
    Assert.assertEquals("[ban-]", onTabComplete("punishments", "pardon", "BAN-"));
  }

  @Test
  public void onTabComplete_oneArg_partialMatch() {
    Assert.assertEquals("[ban-]", onTabComplete("punishments", "pardon", "ba"));
  }

  @Test
  public void onTabComplete_oneArg_partialMatch_withKnownIds() {
    config.getPardonablePunishmentData().addPunishment(sender, "MUTE-6");
    config.getPardonablePunishmentData().addPunishment(sender, "BAN-2");

    Assert.assertEquals("[ban-2]", onTabComplete("punishments", "pardon", "ba"));
  }

  @Test
  public void onTabComplete_oneArg_partialMatch_trailingSpace() {
    Assert.assertEquals("[]", onTabComplete("punishments", "pardon", "ba", ""));
  }

  @Test
  public void onTabComplete_oneArg_noMatches() {
    Assert.assertEquals("[]", onTabComplete("punishments", "pardon", "oops"));
  }

  @Test
  public void onTabComplete_oneArg_matchDefaultOnly() {
    Assert.assertEquals("[ip_ban-]", onTabComplete("punishments", "pardon", "ip"));
  }

  @Test
  public void onTabComplete_oneArg_matchDefaultOnly_whenKnownIdsExist() {
    config.getPardonablePunishmentData().addPunishment(sender, "MUTE-6");

    Assert.assertEquals("[ip_ban-]", onTabComplete("punishments", "pardon", "ip"));
  }

  @Test
  public void onTabComplete_twoArgs_fullMatch_lastArgValidButUnknown() {
    Assert.assertEquals("[]", onTabComplete("punishments", "pardon", "BAN-1", "mute-6"));
  }

  @Test
  public void onTabComplete_twoArgs_fullMatch_lastArgKnown() {
    config.getPardonablePunishmentData().addPunishment(sender, "MUTE-6");

    Assert.assertEquals("[mute-6]", onTabComplete("punishments", "pardon", "BAN-1", "Mute-6"));
  }

  @Test
  public void onTabComplete_twoArgs_fullMatch_trailingSpace() {
    config.getPardonablePunishmentData().addPunishment(sender, "MUTE-6");
    config.getPardonablePunishmentData().addPunishment(sender, "BAN-2");

    Assert.assertEquals("[ban-2]", onTabComplete("punishments", "pardon", "BAN-1", "mute-6", ""));
  }

  @Test
  public void onTabComplete_twoArgs_lastOneMatchesDefaultOnly() {
    Assert.assertEquals("[ip_ban-]", onTabComplete("punishments", "pardon", "ip_ban-123", "ip"));
  }

  @Test
  public void onTabComplete_twoArgs_lastOneMatchesDefaultOnly_withKnownIdsPresent() {
    config.getPardonablePunishmentData().addPunishment(sender, "BAN-2");

    Assert.assertEquals("[ip_ban-]", onTabComplete("punishments", "pardon", "ip_ban-123", "ip"));
  }

  @Test
  public void onTabComplete_twoArgs_partialMatch() {
    Assert.assertEquals("[ban-]", onTabComplete("punishments", "pardon", "ban-1", "ba"));
  }

  @Test
  public void onTabComplete_twoArgs_partialMatch_withKnownIds() {
    config.getPardonablePunishmentData().addPunishment(sender, "IP_BAN-677");
    config.getPardonablePunishmentData().addPunishment(sender, "IP_BAN-678");
    config.getPardonablePunishmentData().addPunishment(sender, "IP_BAN-679");

    Assert.assertEquals("[ip_ban-677, ip_ban-678]", onTabComplete("punishments", "pardon", "ip_ban-679", "ip_ban-67"));
  }

  @Test
  public void onTabComplete_twoArgs_partialMatch_trailingSpace() {
    Assert.assertEquals("[]", onTabComplete("punishments", "pardon", "ban-1", "i", ""));
  }

  @Test
  public void onTabComplete_twoArgs_firstOneInvalid() {
    Assert.assertEquals("[]", onTabComplete("punishments", "pardon", "oops", "ban-"));
  }

  @Test
  public void onTabComplete_twoArgs_secondOneInvalid() {
    Assert.assertEquals("[]", onTabComplete("punishments", "pardon", "ban-1", "oops"));
  }

  @Test
  public void onTabComplete_twoArgs_secondOneInvalid_trailingSpace() {
    Assert.assertEquals("[]", onTabComplete("punishments", "pardon", "ban-1", "oops", ""));
  }

  @Test
  public void onTabComplete_playerSender_noPermissions() {
    config.getPardonablePunishmentData().addPunishment(sender, "BAN-1");
    config.getPardonablePunishmentData().addPunishment(sender, "MUTE-1");
    config.getPardonablePunishmentData().addPunishment(sender, "IP_BAN-1");
    config.getPardonablePunishmentData().addPunishment(sender, "WARNING-1");
    stubPlayerSenderWithPermissions();

    Assert.assertEquals("[]", onTabComplete("punishments", "pardon", ""));
  }

  @Test
  public void onTabComplete_playerSender_banPermissionOnly() {
    stubPlayerSenderWithPermissions(Permission.BAN_IN_GAME);
    config.getPardonablePunishmentData().addPunishment(sender, "BAN-1");
    config.getPardonablePunishmentData().addPunishment(sender, "MUTE-1");
    config.getPardonablePunishmentData().addPunishment(sender, "IP_BAN-1");
    config.getPardonablePunishmentData().addPunishment(sender, "WARNING-1");

    Assert.assertEquals("[ban-1]", onTabComplete("punishments", "pardon", ""));
  }

  @Test
  public void onTabComplete_playerSender_mutePermissionOnly() {
    stubPlayerSenderWithPermissions(Permission.MUTE_IN_GAME);
    config.getPardonablePunishmentData().addPunishment(sender, "BAN-1");
    config.getPardonablePunishmentData().addPunishment(sender, "MUTE-1");
    config.getPardonablePunishmentData().addPunishment(sender, "IP_BAN-1");
    config.getPardonablePunishmentData().addPunishment(sender, "WARNING-1");

    Assert.assertEquals("[mute-1]", onTabComplete("punishments", "pardon", ""));
  }

  @Test
  public void onTabComplete_playerSender_ipBanPermissionOnly() {
    stubPlayerSenderWithPermissions(Permission.IP_BAN_WEB);
    config.getPardonablePunishmentData().addPunishment(sender, "BAN-1");
    config.getPardonablePunishmentData().addPunishment(sender, "MUTE-1");
    config.getPardonablePunishmentData().addPunishment(sender, "IP_BAN-1");
    config.getPardonablePunishmentData().addPunishment(sender, "WARNING-1");

    Assert.assertEquals("[ip_ban-1]", onTabComplete("punishments", "pardon", ""));
  }

  @Test
  public void onTabComplete_playerSender_warningPermissionOnly() {
    stubPlayerSenderWithPermissions(Permission.WARN_WEB);
    config.getPardonablePunishmentData().addPunishment(sender, "BAN-1");
    config.getPardonablePunishmentData().addPunishment(sender, "MUTE-1");
    config.getPardonablePunishmentData().addPunishment(sender, "IP_BAN-1");
    config.getPardonablePunishmentData().addPunishment(sender, "WARNING-1");

    Assert.assertEquals("[warning-1]", onTabComplete("punishments", "pardon", ""));
  }

  @Test
  public void onTabComplete_playerSender_allRelevantPermissions() {
    stubPlayerSenderWithPermissions(Permission.ADMIN);
    config.getPardonablePunishmentData().addPunishment(sender, "BAN-1");
    config.getPardonablePunishmentData().addPunishment(sender, "MUTE-1");
    config.getPardonablePunishmentData().addPunishment(sender, "IP_BAN-1");
    config.getPardonablePunishmentData().addPunishment(sender, "WARNING-1");

    Assert.assertEquals("[ban-1, ip_ban-1, mute-1, warning-1]", onTabComplete("punishments", "pardon", ""));
  }
}
