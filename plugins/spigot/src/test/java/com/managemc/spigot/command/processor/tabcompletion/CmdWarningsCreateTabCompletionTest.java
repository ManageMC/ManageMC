package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.spigot.testutil.TestBase;
import org.junit.Assert;
import org.junit.Test;

public class CmdWarningsCreateTabCompletionTest extends TestBase {

  @Test
  public void onTabComplete_noArgs() {
    stubOnlinePlayers("JimBob123", "hi");

    Assert.assertEquals("[JimBob123, hi]", onTabComplete("warnings", "create", ""));
  }

  @Test
  public void onTabComplete_oneArg_noMatches() {
    stubOnlinePlayers("JimBob123", "hi");

    Assert.assertEquals("[]", onTabComplete("warnings", "create", "oops"));
  }

  @Test
  public void onTabComplete_oneArg_partialMatches() {
    stubOnlinePlayers("Player123", "player456", "irrelevant");

    Assert.assertEquals("[Player123, player456]", onTabComplete("warnings", "create", "pl"));
  }

  @Test
  public void onTabComplete_oneArg_partialMatchesAndOneFullMatch() {
    stubOnlinePlayers("player123", "player1234", "irrelevant");

    Assert.assertEquals("[player123, player1234]", onTabComplete("warnings", "create", "player123"));
  }

  @Test
  public void onTabComplete_oneArg_partialMatches_trailingSpace() {
    stubOnlinePlayers("player123", "Player456", "irrelevant");

    Assert.assertEquals("[[reason]]", onTabComplete("warnings", "create", "pl", ""));
  }

  @Test
  public void onTabComplete_twoArgs() {
    stubOnlinePlayers("player123", "Player456");

    Assert.assertEquals("[]", onTabComplete("warnings", "create", "somebody", "p"));
  }

  @Test
  public void onTabComplete_twoArgs_lastIsSpace() {
    stubOnlinePlayers("player123", "Player456");

    Assert.assertEquals("[[reason]]", onTabComplete("warnings", "create", "somebody", ""));
  }

  @Test
  public void onTabComplete_threeArgs() {
    stubOnlinePlayers("player123", "Player456");

    Assert.assertEquals("[]", onTabComplete("warnings", "create", "a", "s", "d"));
  }

  @Test
  public void onTabComplete_threeArgs_lastIsSpace() {
    stubOnlinePlayers("player123", "Player456");

    Assert.assertEquals("[[details]]", onTabComplete("warnings", "create", "a", "s", ""));
  }
}
