package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.spigot.testutil.TestBase;
import org.junit.Assert;
import org.junit.Test;

public class CmdWatchlistAddCompletionTest extends TestBase {

  @Test
  public void noPlayersOnline_noArgs() {
    Assert.assertEquals("[]", onTabComplete("watchlist", "add", ""));
  }

  @Test
  public void noPlayersOnline_oneArg() {
    Assert.assertEquals("[]", onTabComplete("watchlist", "add", "z"));
  }

  @Test
  public void playersOnline_noMatches() {
    stubOnlinePlayers("aaa", "bbb");

    Assert.assertEquals("[]", onTabComplete("watchlist", "add", "c"));
  }

  @Test
  public void playersOnline_matches() {
    stubOnlinePlayers("asd", "abc", "fgh");

    Assert.assertEquals("[abc, asd]", onTabComplete("watchlist", "add", "a"));
  }
}
