package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.spigot.testutil.TestBase;
import org.junit.Assert;
import org.junit.Test;

public class CmdWatchlistRemoveCompletionTest extends TestBase {

  @Test
  public void noPlayersOnline_noArgs() {
    Assert.assertEquals("[]", onTabComplete("watchlist", "remove", ""));
  }

  @Test
  public void noPlayersOnline_oneArg() {
    Assert.assertEquals("[]", onTabComplete("watchlist", "remove", "z"));
  }

  @Test
  public void playersOnline_noMatches() {
    stubOnlinePlayers("aaa", "bbb");

    Assert.assertEquals("[]", onTabComplete("watchlist", "remove", "c"));
  }

  @Test
  public void playersOnline_matches() {
    stubOnlinePlayers("asd", "abc", "fgh");

    Assert.assertEquals("[abc, asd]", onTabComplete("watchlist", "remove", "a"));
  }
}
