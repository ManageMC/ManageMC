package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.processor.CmdHistory;
import com.managemc.spigot.command.processor.tabcompletion.util.OnlinePlayersTabCompleterTest;

public class CmdHistoryTabCompletionTest extends OnlinePlayersTabCompleterTest {

  @Override
  protected CommandExecutorAsync newCommandExecutor() {
    return new CmdHistory(config);
  }
}
