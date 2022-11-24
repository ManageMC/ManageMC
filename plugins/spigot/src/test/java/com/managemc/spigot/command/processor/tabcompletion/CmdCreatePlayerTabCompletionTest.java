package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.processor.CmdCreatePlayer;
import com.managemc.spigot.testutil.TestBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CmdCreatePlayerTabCompletionTest extends TestBase {

  @Test
  public void tabCompletion() {
    stubOnlinePlayers("asd");

    Assert.assertEquals("[]", tabComplete("players", "create", "as"));
  }

  private String tabComplete(String... args) {
    CommandExecutorAsync cmd = new CmdCreatePlayer(config);
    List<String> completions = cmd.onTabComplete(sender, command, "", args);
    return "[" + String.join(", ", completions) + "]";
  }
}
