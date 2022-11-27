package com.managemc.spigot.command.tabcompletion;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.CmdCreatePlayer;
import com.managemc.spigot.command.tabcompletion.util.TabCompletionTest;
import org.junit.Assert;
import org.junit.Test;

public class CmdCreatePlayerTabCompletionTest extends TabCompletionTest {

  @Test
  public void tabCompletion() {
    stubOnlinePlayers("asd");

    Assert.assertEquals("[]", tabComplete("players", "create", "as"));
  }

  @Override
  protected CommandExecutorAsync newCommandExecutor() {
    return new CmdCreatePlayer(config);
  }
}
