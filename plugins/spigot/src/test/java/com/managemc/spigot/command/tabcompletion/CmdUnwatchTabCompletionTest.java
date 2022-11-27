package com.managemc.spigot.command.tabcompletion;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.CmdUnwatch;
import com.managemc.spigot.command.tabcompletion.util.OnlinePlayersTabCompleterTest;
import org.junit.Assert;
import org.junit.Test;

public class CmdUnwatchTabCompletionTest extends OnlinePlayersTabCompleterTest {

  @Test
  public void noPermission() {
    stubOnlinePlayers("aaa");
    stubPlayerSenderWithPermissions();
    Assert.assertEquals("[]", tabComplete("a"));
  }

  @Override
  protected CommandExecutorAsync newCommandExecutor() {
    return new CmdUnwatch(config);
  }
}
