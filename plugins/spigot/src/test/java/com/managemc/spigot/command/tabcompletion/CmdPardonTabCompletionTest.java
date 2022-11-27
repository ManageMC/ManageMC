package com.managemc.spigot.command.tabcompletion;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.CmdPardon;
import com.managemc.spigot.command.tabcompletion.util.TabCompletionTest;
import com.managemc.spigot.util.permissions.Permission;
import org.junit.Assert;
import org.junit.Test;

public class CmdPardonTabCompletionTest extends TabCompletionTest {

  @Test
  public void noArgs() {
    Assert.assertEquals("[ban-, ip_ban-, mute-, warning-]", tabComplete(""));
  }

  @Test
  public void noArgs_withKnownIds() {
    config.getPardonablePunishmentData().addPunishment(sender, "MUTE-6");
    config.getPardonablePunishmentData().addPunishment(sender, "BAN-2");

    Assert.assertEquals("[ban-2, mute-6]", tabComplete(""));
  }

  @Test
  public void noArgs_trailingSpace() {
    Assert.assertEquals("[ban-, ip_ban-, mute-, warning-]", tabComplete(""));
  }

  @Test
  public void noArgs_trailingSpace_withKnownIds() {
    config.getPardonablePunishmentData().addPunishment(sender, "MUTE-6");
    config.getPardonablePunishmentData().addPunishment(sender, "BAN-2");

    Assert.assertEquals("[ban-2, mute-6]", tabComplete(""));
  }

  @Test
  public void oneArg_fullMatch() {
    Assert.assertEquals("[ban-]", tabComplete("ban-"));
  }

  @Test
  public void oneArg_fullMatch_differentCase() {
    Assert.assertEquals("[ban-]", tabComplete("BAN-"));
  }

  @Test
  public void oneArg_partialMatch() {
    Assert.assertEquals("[ban-]", tabComplete("ba"));
  }

  @Test
  public void oneArg_partialMatch_withKnownIds() {
    config.getPardonablePunishmentData().addPunishment(sender, "MUTE-6");
    config.getPardonablePunishmentData().addPunishment(sender, "BAN-2");

    Assert.assertEquals("[ban-2]", tabComplete("ba"));
  }

  @Test
  public void oneArg_partialMatch_trailingSpace() {
    Assert.assertEquals("[]", tabComplete("ba", ""));
  }

  @Test
  public void oneArg_noMatches() {
    Assert.assertEquals("[]", tabComplete("oops"));
  }

  @Test
  public void oneArg_matchDefaultOnly() {
    Assert.assertEquals("[ip_ban-]", tabComplete("ip"));
  }

  @Test
  public void oneArg_matchDefaultOnly_whenKnownIdsExist() {
    config.getPardonablePunishmentData().addPunishment(sender, "MUTE-6");

    Assert.assertEquals("[ip_ban-]", tabComplete("ip"));
  }

  @Test
  public void twoArgs_fullMatch_lastArgValidButUnknown() {
    Assert.assertEquals("[]", tabComplete("BAN-1", "mute-6"));
  }

  @Test
  public void twoArgs_fullMatch_lastArgKnown() {
    config.getPardonablePunishmentData().addPunishment(sender, "MUTE-6");

    Assert.assertEquals("[mute-6]", tabComplete("BAN-1", "Mute-6"));
  }

  @Test
  public void twoArgs_fullMatch_trailingSpace() {
    config.getPardonablePunishmentData().addPunishment(sender, "MUTE-6");
    config.getPardonablePunishmentData().addPunishment(sender, "BAN-2");

    Assert.assertEquals("[ban-2]", tabComplete("BAN-1", "mute-6", ""));
  }

  @Test
  public void twoArgs_lastOneMatchesDefaultOnly() {
    Assert.assertEquals("[ip_ban-]", tabComplete("ip_ban-123", "ip"));
  }

  @Test
  public void twoArgs_lastOneMatchesDefaultOnly_withKnownIdsPresent() {
    config.getPardonablePunishmentData().addPunishment(sender, "BAN-2");

    Assert.assertEquals("[ip_ban-]", tabComplete("ip_ban-123", "ip"));
  }

  @Test
  public void twoArgs_partialMatch() {
    Assert.assertEquals("[ban-]", tabComplete("ban-1", "ba"));
  }

  @Test
  public void twoArgs_partialMatch_withKnownIds() {
    config.getPardonablePunishmentData().addPunishment(sender, "IP_BAN-677");
    config.getPardonablePunishmentData().addPunishment(sender, "IP_BAN-678");
    config.getPardonablePunishmentData().addPunishment(sender, "IP_BAN-679");

    Assert.assertEquals("[ip_ban-677, ip_ban-678]", tabComplete("ip_ban-679", "ip_ban-67"));
  }

  @Test
  public void twoArgs_partialMatch_trailingSpace() {
    Assert.assertEquals("[]", tabComplete("ban-1", "i", ""));
  }

  @Test
  public void twoArgs_firstOneInvalid() {
    Assert.assertEquals("[]", tabComplete("oops", "ban-"));
  }

  @Test
  public void twoArgs_secondOneInvalid() {
    Assert.assertEquals("[]", tabComplete("ban-1", "oops"));
  }

  @Test
  public void twoArgs_secondOneInvalid_trailingSpace() {
    Assert.assertEquals("[]", tabComplete("ban-1", "oops", ""));
  }

  @Test
  public void playerSender_noPermissions() {
    config.getPardonablePunishmentData().addPunishment(sender, "BAN-1");
    config.getPardonablePunishmentData().addPunishment(sender, "MUTE-1");
    config.getPardonablePunishmentData().addPunishment(sender, "IP_BAN-1");
    config.getPardonablePunishmentData().addPunishment(sender, "WARNING-1");
    stubPlayerSenderWithPermissions();

    Assert.assertEquals("[]", tabComplete(""));
  }

  @Test
  public void playerSender_banPermissionOnly() {
    stubPlayerSenderWithPermissions(Permission.BAN_IN_GAME);
    config.getPardonablePunishmentData().addPunishment(sender, "BAN-1");
    config.getPardonablePunishmentData().addPunishment(sender, "MUTE-1");
    config.getPardonablePunishmentData().addPunishment(sender, "IP_BAN-1");
    config.getPardonablePunishmentData().addPunishment(sender, "WARNING-1");

    Assert.assertEquals("[ban-1]", tabComplete(""));
  }

  @Test
  public void playerSender_mutePermissionOnly() {
    stubPlayerSenderWithPermissions(Permission.MUTE_IN_GAME);
    config.getPardonablePunishmentData().addPunishment(sender, "BAN-1");
    config.getPardonablePunishmentData().addPunishment(sender, "MUTE-1");
    config.getPardonablePunishmentData().addPunishment(sender, "IP_BAN-1");
    config.getPardonablePunishmentData().addPunishment(sender, "WARNING-1");

    Assert.assertEquals("[mute-1]", tabComplete(""));
  }

  @Test
  public void playerSender_ipBanPermissionOnly() {
    stubPlayerSenderWithPermissions(Permission.IP_BAN_WEB);
    config.getPardonablePunishmentData().addPunishment(sender, "BAN-1");
    config.getPardonablePunishmentData().addPunishment(sender, "MUTE-1");
    config.getPardonablePunishmentData().addPunishment(sender, "IP_BAN-1");
    config.getPardonablePunishmentData().addPunishment(sender, "WARNING-1");

    Assert.assertEquals("[ip_ban-1]", tabComplete(""));
  }

  @Test
  public void playerSender_warningPermissionOnly() {
    stubPlayerSenderWithPermissions(Permission.WARN_WEB);
    config.getPardonablePunishmentData().addPunishment(sender, "BAN-1");
    config.getPardonablePunishmentData().addPunishment(sender, "MUTE-1");
    config.getPardonablePunishmentData().addPunishment(sender, "IP_BAN-1");
    config.getPardonablePunishmentData().addPunishment(sender, "WARNING-1");

    Assert.assertEquals("[warning-1]", tabComplete(""));
  }

  @Test
  public void playerSender_allRelevantPermissions() {
    stubPlayerSenderWithPermissions(Permission.ADMIN);
    config.getPardonablePunishmentData().addPunishment(sender, "BAN-1");
    config.getPardonablePunishmentData().addPunishment(sender, "MUTE-1");
    config.getPardonablePunishmentData().addPunishment(sender, "IP_BAN-1");
    config.getPardonablePunishmentData().addPunishment(sender, "WARNING-1");

    Assert.assertEquals("[ban-1, ip_ban-1, mute-1, warning-1]", tabComplete(""));
  }


  @Override
  protected CommandExecutorAsync newCommandExecutor() {
    return new CmdPardon(config);
  }
}
