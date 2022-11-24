package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.processor.CmdWatch;
import com.managemc.spigot.testutil.TestBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CmdWatchCompletionTest extends TestBase {

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
    stubOnlinePlayers("asd", "abc", "fgh");

    Assert.assertEquals("[abc, asd]", tabComplete("a"));
  }


  private String tabComplete(String... args) {
    CommandExecutorAsync cmd = new CmdWatch(config);
    List<String> completions = cmd.onTabComplete(sender, command, "", args);
    return "[" + String.join(", ", completions) + "]";
  }
}
