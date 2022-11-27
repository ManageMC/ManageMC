package com.managemc.spigot.command.tabcompletion.util;

import org.junit.Assert;
import org.junit.Test;

public abstract class OnlinePlayersTabCompleterTest extends TabCompletionTest {

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
  public void twoArgs() {
    stubOnlinePlayers("bac", "abc", "fgh");

    Assert.assertEquals("[]", tabComplete("a", "a"));
  }
}
