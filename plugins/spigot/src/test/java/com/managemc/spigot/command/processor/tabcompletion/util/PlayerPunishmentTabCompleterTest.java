package com.managemc.spigot.command.processor.tabcompletion.util;

import com.managemc.spigot.util.permissions.Permission;
import org.junit.Assert;
import org.junit.Test;

public abstract class PlayerPunishmentTabCompleterTest extends TabCompletionTest {

  @Test
  public void noPlayersOnline_noArgs() {
    Assert.assertEquals("[]", tabComplete(""));
  }

  @Test
  public void noPlayersOnline_oneArg() {
    Assert.assertEquals("[]", tabComplete("z"));
  }

  @Test
  public void playersOnline_noMatches() {
    stubOnlinePlayers("aaa", "bbb");

    Assert.assertEquals("[]", tabComplete("c"));
  }

  @Test
  public void playersOnline_matches() {
    stubOnlinePlayers("BAC", "abc", "fgh");

    Assert.assertEquals("[abc, BAC, fgh]", tabComplete(""));
    Assert.assertEquals("[abc, BAC]", tabComplete("a"));
  }


  @Test
  public void oneArg() {
    Assert.assertEquals("[--broadcast, --duration=, --local, --private, --shadow]", tabComplete("bob123", ""));
  }

  @Test
  public void twoArgs_noSpace() {
    Assert.assertEquals("[]", tabComplete("bob123", "eh"));
  }

  @Test
  public void twoArgs_noSpace_durationFlag() {
    Assert.assertEquals("[--duration=]", tabComplete("bob123", "--duration"));
  }

  @Test
  public void twoArgs_noSpace_durationFlagAlias() {
    Assert.assertEquals("[--duration=]", tabComplete("bob123", "-d"));
  }

  @Test
  public void twoArgs() {
    Assert.assertEquals("[--broadcast, --duration=, --local, --private, --shadow]", tabComplete("bob123", "eh", ""));
  }

  @Test
  public void twoArgs_durationFlagValid_trailingSpace() {
    Assert.assertEquals("[--broadcast, --local, --private, --shadow]", tabComplete("bob123", "--duration=24h", ""));
  }

  @Test
  public void twoArgs_durationFlagAliasValid_trailingSpace() {
    Assert.assertEquals("[--broadcast, --local, --private, --shadow]", tabComplete("bob123", "-d=", ""));
  }

  @Test
  public void threeArgs_noSpace() {
    Assert.assertEquals("[]", tabComplete("bob123", "eh", "meh"));
  }

  @Test
  public void threeArgs_noSpace_durationFlagIncomplete() {
    Assert.assertEquals("[--duration=]", tabComplete("bob123", "eh", "--duration"));
  }

  @Test
  public void threeArgs_noSpace_durationFlagComplete() {
    Assert.assertEquals("[]", tabComplete("bob123", "eh", "--duration="));
  }

  @Test
  public void threeArgs_lastIsPartialMatch() {
    Assert.assertEquals("[--broadcast, --duration=, --local, --private, --shadow]", tabComplete("bob123", "eh", "--"));
  }

  @Test
  public void threeArgs_lastIsDetails() {
    Assert.assertEquals("[--broadcast, --duration=, --local, --private, --shadow]", tabComplete("bob123", "eh", "meh", ""));
  }

  @Test
  public void threeArgs_lastIsFlag() {
    Assert.assertEquals("[--broadcast, --duration=, --private, --shadow]", tabComplete("bob123", "eh", "meh", "--local", ""));
  }

  @Test
  public void threeArgs_lastIsFlagAlias() {
    Assert.assertEquals("[--broadcast, --duration=, --private, --shadow]", tabComplete("bob123", "eh", "meh", "-l", ""));
  }

  @Test
  public void incompatibleFlags() {
    Assert.assertEquals("[--duration=, --local]", tabComplete("bob123", "eh", "meh", "-p", ""));
    Assert.assertEquals("[--duration=, --local]", tabComplete("bob123", "eh", "meh", "--shadow", ""));
    Assert.assertEquals("[--duration=, --local]", tabComplete("bob123", "eh", "meh", "-b", ""));
  }

  @Test
  public void threeArgs_durationFlagValid_trailingSpace() {
    Assert.assertEquals("[--broadcast, --local, --private, --shadow]", tabComplete("bob123", "asd", "--duration=24h", "meh", ""));
  }

  @Test
  public void threeArgs_durationFlagInvalid_trailingSpace() {
    Assert.assertEquals("[--broadcast, --duration=, --local, --private, --shadow]", tabComplete("bob123", "asd", "--duration", "meh", ""));
  }

  @Test
  public void threeArgs_durationFlagAlias_noTrailingSpace() {
    Assert.assertEquals("[]", tabComplete("bob123", "asd", "-d=5h"));
  }

  @Test
  public void threeArgs_durationFlagAlias_trailingSpace() {
    Assert.assertEquals("[--broadcast, --local, --private, --shadow]", tabComplete("bob123", "asd", "-d=5h", ""));
  }

  @Test
  public void fourArgs_noSpace() {
    Assert.assertEquals("[]", tabComplete("bob123", "eh", "meh", "hmm"));
  }

  @Test
  public void fourArgs() {
    Assert.assertEquals("[--broadcast, --duration=, --local, --private, --shadow]", tabComplete("bob123", "meh", "meh", "hmm", ""));
  }

  @Test
  public void playerSender_withNoPermissions() {
    stubPlayerSenderWithPermissions();
    Assert.assertEquals("[]", tabComplete("--"));
  }

  @Test
  public void playerSender_withoutShadowPermission() {
    stubPlayerSenderWithPermissions(Permission.BAN_IN_GAME, Permission.MUTE_IN_GAME, Permission.WARN_IN_GAME, Permission.IP_BAN_IN_GAME);
    Assert.assertEquals("[--broadcast, --duration=, --local, --private]", tabComplete("asd", "--"));
  }

  @Test
  public void playerSender_withShadowPermission() {
    stubPlayerSenderWithPermissions(Permission.PUNISHMENTS_SHADOW, Permission.BAN_IN_GAME, Permission.MUTE_IN_GAME, Permission.WARN_IN_GAME, Permission.IP_BAN_IN_GAME);
    Assert.assertEquals("[--broadcast, --duration=, --local, --private, --shadow]", tabComplete("asd", "--"));
  }
}
