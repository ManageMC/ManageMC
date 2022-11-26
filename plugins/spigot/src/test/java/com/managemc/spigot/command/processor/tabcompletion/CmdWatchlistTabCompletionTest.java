package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.processor.CmdWatchlist;
import com.managemc.spigot.command.processor.tabcompletion.util.TabCompletionTest;
import org.junit.Assert;
import org.junit.Test;

public class CmdWatchlistTabCompletionTest extends TabCompletionTest {

  @Test
  public void tabCompletion() {
    stubOnlinePlayers("asd");

    Assert.assertEquals("[]", tabComplete("as"));
  }

  @Override
  protected CommandExecutorAsync newCommandExecutor() {
    return new CmdWatchlist(config);
  }
}
