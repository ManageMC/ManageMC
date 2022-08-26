package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.spigot.testutil.TestBase;
import com.managemc.spigot.util.permissions.Permission;
import org.junit.Assert;
import org.junit.Test;

public class CmdBansCreateTabCompletionTest extends TestBase {

  @Test
  public void onTabComplete_noArgs() {
    stubOnlinePlayers();
    Assert.assertEquals("[]", onTabComplete("bans", "create", ""));
  }

  @Test
  public void onTabComplete_noArgs_withOnlinePlayers() {
    stubOnlinePlayers("boB", "sue");
    Assert.assertEquals("[boB, sue]", onTabComplete("bans", "create", ""));
  }

  @Test
  public void onTabComplete_noArgs_withSpace() {
    stubOnlinePlayers();
    Assert.assertEquals("[]", onTabComplete("bans", "create", ""));
  }

  @Test
  public void onTabComplete_noArgs_withSpace_withOnlinePlayers() {
    stubOnlinePlayers("boB", "sue");
    Assert.assertEquals("[boB, sue]", onTabComplete("bans", "create", ""));
  }

  @Test
  public void onTabComplete_oneArg_noSpace() {
    stubOnlinePlayers();
    Assert.assertEquals("[]", onTabComplete("bans", "create", "jacob"));
  }

  @Test
  public void onTabComplete_oneArg_noSpace_partialMatch() {
    stubOnlinePlayers("JacobCrofts", "random_person", "jacob123");
    Assert.assertEquals("[JacobCrofts, jacob123]", onTabComplete("bans", "create", "jacob"));
  }

  @Test
  public void onTabComplete_oneArg() {
    Assert.assertEquals("[[reason]]", onTabComplete("bans", "create", "bob123", ""));
  }

  @Test
  public void onTabComplete_twoArgs_noSpace() {
    Assert.assertEquals("[]", onTabComplete("bans", "create", "bob123", "eh"));
  }

  @Test
  public void onTabComplete_twoArgs_noSpace_durationFlag() {
    Assert.assertEquals("[--duration=]", onTabComplete("bans", "create", "bob123", "--duration"));
  }

  @Test
  public void onTabComplete_twoArgs_noSpace_durationFlagAlias() {
    Assert.assertEquals("[--duration=]", onTabComplete("bans", "create", "bob123", "-d"));
  }

  @Test
  public void onTabComplete_twoArgs() {
    Assert.assertEquals("[[details]]", onTabComplete("bans", "create", "bob123", "eh", ""));
  }

  @Test
  public void onTabComplete_twoArgs_durationFlagInvalid_trailingSpace() {
    Assert.assertEquals("[[details]]", onTabComplete("bans", "create", "bob123", "--duration", ""));
  }

  @Test
  public void onTabComplete_twoArgs_durationFlagValid_trailingSpace() {
    Assert.assertEquals("[[reason]]", onTabComplete("bans", "create", "bob123", "--duration=24h", ""));
  }

  @Test
  public void onTabComplete_twoArgs_durationFlagAliasInvalid_trailingSpace() {
    Assert.assertEquals("[[details]]", onTabComplete("bans", "create", "bob123", "-d", ""));
  }

  @Test
  public void onTabComplete_twoArgs_durationFlagAliasValid_trailingSpace() {
    Assert.assertEquals("[[reason]]", onTabComplete("bans", "create", "bob123", "-d=", ""));
  }

  @Test
  public void onTabComplete_threeArgs_noSpace() {
    Assert.assertEquals("[]", onTabComplete("bans", "create", "bob123", "eh", "meh"));
  }

  @Test
  public void onTabComplete_threeArgs_noSpace_durationFlagIncomplete() {
    Assert.assertEquals("[--duration=]", onTabComplete("bans", "create", "bob123", "eh", "--duration"));
  }

  @Test
  public void onTabComplete_threeArgs_noSpace_durationFlagComplete() {
    Assert.assertEquals("[]", onTabComplete("bans", "create", "bob123", "eh", "--duration="));
  }

  @Test
  public void onTabComplete_threeArgs_lastIsPartialMatch() {
    Assert.assertEquals("[--broadcast, --duration=, --local, --private, --shadow]", onTabComplete("bans", "create", "bob123", "eh", "--"));
  }

  @Test
  public void onTabComplete_threeArgs_lastIsDetails() {
    Assert.assertEquals("[--broadcast, --duration=, --local, --private, --shadow]", onTabComplete("bans", "create", "bob123", "eh", "meh", ""));
  }

  @Test
  public void onTabComplete_threeArgs_lastIsFlag() {
    Assert.assertEquals("[--broadcast, --duration=, --private, --shadow]", onTabComplete("bans", "create", "bob123", "eh", "meh", "--local", ""));
  }

  @Test
  public void onTabComplete_threeArgs_lastIsFlagAlias() {
    Assert.assertEquals("[--broadcast, --duration=, --private, --shadow]", onTabComplete("bans", "create", "bob123", "eh", "meh", "-l", ""));
  }

  @Test
  public void incompatibleFlags() {
    Assert.assertEquals("[--duration=, --local]", onTabComplete("bans", "create", "bob123", "eh", "meh", "-p", ""));
    Assert.assertEquals("[--duration=, --local]", onTabComplete("bans", "create", "bob123", "eh", "meh", "--shadow", ""));
    Assert.assertEquals("[--duration=, --local]", onTabComplete("bans", "create", "bob123", "eh", "meh", "-b", ""));
  }

  @Test
  public void onTabComplete_threeArgs_durationFlagValid_trailingSpace() {
    Assert.assertEquals("[--broadcast, --local, --private, --shadow]", onTabComplete("bans", "create", "bob123", "asd", "--duration=24h", "meh", ""));
  }

  @Test
  public void onTabComplete_threeArgs_durationFlagInvalid_trailingSpace() {
    Assert.assertEquals("[--broadcast, --duration=, --local, --private, --shadow]", onTabComplete("bans", "create", "bob123", "asd", "--duration", "meh", ""));
  }

  @Test
  public void onTabComplete_threeArgs_durationFlagAlias_noTrailingSpace() {
    Assert.assertEquals("[]", onTabComplete("bans", "create", "bob123", "asd", "-d=5h"));
  }

  @Test
  public void onTabComplete_threeArgs_durationFlagAlias_trailingSpace() {
    Assert.assertEquals("[[details]]", onTabComplete("bans", "create", "bob123", "asd", "-d=5h", ""));
  }

  @Test
  public void onTabComplete_fourArgs_noSpace() {
    Assert.assertEquals("[]", onTabComplete("bans", "create", "bob123", "eh", "meh", "hmm"));
  }

  @Test
  public void onTabComplete_fourArgs() {
    Assert.assertEquals("[--broadcast, --duration=, --local, --private, --shadow]", onTabComplete("bans", "create", "bob123", "meh", "meh", "hmm", ""));
  }

  @Test
  public void playerSender_withNoPermissions() {
    stubPlayerSenderWithPermissions();
    Assert.assertEquals("[]", onTabComplete("bans", "create", "--"));
  }

  @Test
  public void playerSender_withoutShadowPermission() {
    stubPlayerSenderWithPermissions(Permission.BAN_IN_GAME);
    Assert.assertEquals("[--broadcast, --duration=, --local, --private]", onTabComplete("bans", "create", "--"));
  }

  @Test
  public void playerSender_withShadowPermission() {
    stubPlayerSenderWithPermissions(Permission.BAN_IN_GAME, Permission.PUNISHMENTS_SHADOW);
    Assert.assertEquals("[--broadcast, --duration=, --local, --private, --shadow]", onTabComplete("bans", "create", "--"));
  }
}
