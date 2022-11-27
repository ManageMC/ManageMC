package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.processor.CmdWarn;
import com.managemc.spigot.command.processor.tabcompletion.util.OnlinePlayersTabCompleterTest;
import org.junit.Assert;
import org.junit.Test;

public class CmdWarnTabCompletionTest extends OnlinePlayersTabCompleterTest {

  @Test
  public void noPermission() {
    stubOnlinePlayers("aaa");
    stubPlayerSenderWithPermissions();
    Assert.assertEquals("[]", tabComplete("a"));
  }

  @Override
  protected CommandExecutorAsync newCommandExecutor() {
    return new CmdWarn(config);
  }
}
