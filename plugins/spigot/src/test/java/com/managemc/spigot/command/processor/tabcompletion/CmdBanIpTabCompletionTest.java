package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.processor.CmdBanIp;
import com.managemc.spigot.testutil.TestBase;
import com.managemc.spigot.util.permissions.Permission;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class CmdBanIpTabCompletionTest extends TestBase {

  @Before
  public void setup() {
    stubOnlinePlayers("asd", "abc", "sdf");
  }

  @Test
  public void noArgs() {
    Assert.assertEquals("[abc, asd, sdf]", tabComplete());
  }

  @Test
  public void noArgs_playersOnline() {
    Assert.assertEquals("[abc, asd, sdf]", tabComplete(""));
  }

  @Test
  public void oneArg_matchingPlayers() {
    Assert.assertEquals("[abc]", tabComplete("b"));
  }

  @Test
  public void oneArg_noMatchingPlayers() {
    Assert.assertEquals("[]", tabComplete("z"));
  }

  @Test
  public void twoArgs_lastIsSpace() {
    Assert.assertEquals("[--broadcast, --duration=, --local, --private, --shadow]", tabComplete("bob123", ""));
  }

  @Test
  public void twoArgs_noSpace() {
    Assert.assertEquals("[]", tabComplete("bob123", "eh"));
  }

  @Test
  public void twoArgs_flagIncomplete() {
    Assert.assertEquals("[--duration=]", tabComplete("bob123", "--duration"));
  }

  @Test
  public void twoArgs_flagComplete() {
    Assert.assertEquals("[]", tabComplete("bob123", "--duration=24h"));
  }

  @Test
  public void twoArgs_flagIncomplete_withAlias() {
    Assert.assertEquals("[]", tabComplete("bob123", "-d="));
  }

  @Test
  public void invalidFlag_trailingSpace() {
    Assert.assertEquals("[--broadcast, --local, --private, --shadow]", tabComplete("bob123", "--duration=", ""));
  }

  @Test
  public void invalidFlag_trailingSpace_withAlias() {
    Assert.assertEquals("[--broadcast, --local, --private, --shadow]", tabComplete("bob123", "-d=", ""));
  }

  @Test
  public void lastArgIsIncompleteFlag() {
    Assert.assertEquals("[--duration=]", tabComplete("bob123", "eh", "--duration"));
  }

  @Test
  public void lastArgIsCompleteFlag() {
    Assert.assertEquals("[]", tabComplete("bob123", "eh", "--duration=2h"));
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
    stubPlayerSenderWithPermissions(Permission.IP_BAN_IN_GAME);
    Assert.assertEquals("[--broadcast, --duration=, --local, --private]", tabComplete("asd", "--"));
  }

  @Test
  public void playerSender_withShadowPermission() {
    stubPlayerSenderWithPermissions(Permission.IP_BAN_IN_GAME, Permission.PUNISHMENTS_SHADOW);
    Assert.assertEquals("[--broadcast, --duration=, --local, --private, --shadow]", tabComplete("asd", "--"));
  }


  private String tabComplete(String... args) {
    CommandExecutorAsync cmd = new CmdBanIp(config);
    List<String> completions = cmd.onTabComplete(sender, command, "", args);
    return "[" + String.join(", ", completions) + "]";
  }
}
