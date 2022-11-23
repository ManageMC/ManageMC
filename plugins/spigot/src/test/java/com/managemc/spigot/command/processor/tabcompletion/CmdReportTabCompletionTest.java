package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.spigot.testutil.TestBase;
import org.junit.Assert;
import org.junit.Test;

public class CmdReportTabCompletionTest extends TestBase {

  @Test
  public void noPlayersOnline_noArgs() {
    Assert.assertEquals("[]", onTabComplete("reports", "create", ""));
  }

  @Test
  public void noPlayersOnline_oneArg() {
    Assert.assertEquals("[]", onTabComplete("reports", "create", "z"));
  }

  @Test
  public void playersOnline_noMatches() {
    stubOnlinePlayers("aaa", "bbb");

    Assert.assertEquals("[]", onTabComplete("reports", "create", "c"));
  }

  @Test
  public void playersOnline_matches() {
    stubOnlinePlayers("asd", "abc", "fgh");

    Assert.assertEquals("[abc, asd]", onTabComplete("reports", "create", "a"));
  }

  @Test
  public void playersOnline_tooManyArgs() {
    stubOnlinePlayers("asd", "abc", "fgh");

    Assert.assertEquals("[]", onTabComplete("reports", "create", "abc", "a"));
  }
}
