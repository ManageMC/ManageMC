package com.managemc.spigot.command.tabcompletion;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.CmdHistory;
import com.managemc.spigot.command.tabcompletion.util.OnlinePlayersTabCompleterTest;
import org.junit.Assert;
import org.junit.Test;

public class CmdHistoryTabCompletionTest extends OnlinePlayersTabCompleterTest {

  @Test
  public void noPermission() {
    stubOnlinePlayers("aaa");
    stubPlayerSenderWithPermissions();
    Assert.assertEquals("[]", tabComplete("a"));
  }

  @Override
  protected CommandExecutorAsync newCommandExecutor() {
    return new CmdHistory(config);
  }
}
