package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.processor.CmdHistory;
import com.managemc.spigot.testutil.TestBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CmdHistoryTabCompletionTest extends TestBase {

  @Test
  public void tabComplete_noArgs_noPlayersOnline() {
    stubOnlinePlayers();

    Assert.assertEquals("[]", tabComplete(""));
  }

  @Test
  public void tabComplete_noArgs_withOnlinePlayers() {
    // these should be sorted alphabetically in the end
    stubOnlinePlayers("a", "b", "c");

    Assert.assertEquals("[a, b, c]", tabComplete(""));
  }

  @Test
  public void tabComplete_oneArg_noMatches() {
    stubOnlinePlayers("a", "b", "c");

    Assert.assertEquals("[]", tabComplete("asd"));
  }

  @Test
  public void tabComplete_oneArg_partialMatches_differentCases() {
    stubOnlinePlayers("JOE", "BOB", "SUE");
    Assert.assertEquals("[BOB, JOE]", tabComplete("o"));

    stubOnlinePlayers("bob", "joe", "sue");
    Assert.assertEquals("[bob, joe]", tabComplete("O"));
  }

  @Test
  public void tabComplete_oneArg_partialMatches_trailingSpace() {
    stubOnlinePlayers("123", "124", "345");

    Assert.assertEquals("[]", tabComplete("4", ""));
  }

  @Test
  public void tabComplete_twoArgs_whichIsInvalid() {
    stubOnlinePlayers("123", "124", "345");

    Assert.assertEquals("[]", tabComplete("4", "2"));
  }


  private String tabComplete(String... args) {
    CommandExecutorAsync cmd = new CmdHistory(config);
    List<String> completions = cmd.onTabComplete(sender, command, "", args);
    return "[" + String.join(", ", completions) + "]";
  }
}
