package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.processor.CmdWarn;
import com.managemc.spigot.testutil.TestBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CmdWarnTabCompletionTest extends TestBase {

  @Test
  public void onTabComplete_noArgs() {
    stubOnlinePlayers("JimBob123", "hi");

    Assert.assertEquals("[JimBob123, hi]", tabComplete(""));
  }

  @Test
  public void onTabComplete_oneArg_noMatches() {
    stubOnlinePlayers("JimBob123", "hi");

    Assert.assertEquals("[]", tabComplete("oops"));
  }

  @Test
  public void onTabComplete_oneArg_partialMatches() {
    stubOnlinePlayers("Player123", "player456", "irrelevant");

    Assert.assertEquals("[Player123, player456]", tabComplete("pl"));
  }

  @Test
  public void onTabComplete_oneArg_partialMatchesAndOneFullMatch() {
    stubOnlinePlayers("player123", "player1234", "irrelevant");

    Assert.assertEquals("[player123, player1234]", tabComplete("player123"));
  }

  @Test
  public void onTabComplete_twoArgs() {
    stubOnlinePlayers("player123", "Player456");

    Assert.assertEquals("[]", tabComplete("somebody", "p"));
  }

  @Test
  public void onTabComplete_threeArgs() {
    stubOnlinePlayers("player123", "Player456");

    Assert.assertEquals("[]", tabComplete("a", "s", "d"));
  }


  private String tabComplete(String... args) {
    CommandExecutorAsync cmd = new CmdWarn(config);
    List<String> completions = cmd.onTabComplete(sender, command, "", args);
    return "[" + String.join(", ", completions) + "]";
  }
}
