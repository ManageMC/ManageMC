package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.spigot.testutil.TestBase;
import org.junit.Assert;
import org.junit.Test;

public class CmdHistoryTabCompletionTest extends TestBase {

  @Test
  public void tabComplete_noArgs_noPlayersOnline() {
    stubOnlinePlayers();

    Assert.assertEquals("[]", onTabComplete("punishments", "get", ""));
  }

  @Test
  public void tabComplete_noArgs_withOnlinePlayers() {
    // these should be sorted alphabetically in the end
    stubOnlinePlayers("a", "b", "c");

    Assert.assertEquals("[a, b, c]", onTabComplete("punishments", "get", ""));
  }

  @Test
  public void tabComplete_oneArg_noMatches() {
    stubOnlinePlayers("a", "b", "c");

    Assert.assertEquals("[]", onTabComplete("punishments", "get", "asd"));
  }

  @Test
  public void tabComplete_oneArg_partialMatches_differentCases() {
    stubOnlinePlayers("JOE", "BOB", "SUE");
    Assert.assertEquals("[BOB, JOE]", onTabComplete("punishments", "get", "o"));

    stubOnlinePlayers("bob", "joe", "sue");
    Assert.assertEquals("[bob, joe]", onTabComplete("punishments", "get", "O"));
  }

  @Test
  public void tabComplete_oneArg_partialMatches_trailingSpace() {
    stubOnlinePlayers("123", "124", "345");

    Assert.assertEquals("[]", onTabComplete("punishments", "get", "4", ""));
  }

  @Test
  public void tabComplete_twoArgs_whichIsInvalid() {
    stubOnlinePlayers("123", "124", "345");

    Assert.assertEquals("[]", onTabComplete("punishments", "get", "4", "2"));
  }
}
